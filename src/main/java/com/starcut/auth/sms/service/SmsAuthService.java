package com.starcut.auth.sms.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.starcut.auth.sms.config.SmsAuthConfig;
import com.starcut.auth.sms.db.PhoneUuidRepository;
import com.starcut.auth.sms.db.PhonenumberLockRepository;
import com.starcut.auth.sms.db.SmsCodeRepository;
import com.starcut.auth.sms.db.entity.PhoneUuid;
import com.starcut.auth.sms.db.entity.PhonenumberLock;
import com.starcut.auth.sms.db.entity.SmsCode;
import com.starcut.auth.sms.db.entity.SmsCodeId;
import com.starcut.auth.sms.db.entity.type.SmsCodeType;
import com.starcut.auth.sms.exceptions.ExpiredCodeException;
import com.starcut.auth.sms.exceptions.InvalidCodeException;
import com.starcut.auth.sms.exceptions.InvalidPhoneNumberException;
import com.starcut.auth.sms.exceptions.TooManySmsSentException;
import com.starcut.auth.sms.exceptions.TooManyTrialsException;
import com.starcut.auth.sms.exceptions.WrongCodeException;

@Service
public class SmsAuthService {

	@Autowired
	private SmsCodeRepository smsCodeRepository;

	@Autowired
	private PhonenumberLockRepository phonenumberLockRepository;

	@Autowired
	private PhoneUuidRepository phoneUuidRepository;

	@Autowired
	private SmsSenderService smsSenderService;

	private SmsAuthConfig smsAuthConfig;

	private SecureRandom secureRandom = new SecureRandom();

	private PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public SmsAuthService(SmsAuthConfig authSmsConfig) {
		this.smsAuthConfig = authSmsConfig;
	}

	private String generateCode() {
		Long numberOfCodes = (long) Math.pow(10, smsAuthConfig.getCodeLength());
		Long code = Math.abs(secureRandom.nextLong()) % numberOfCodes;
		return String.format("%0" + smsAuthConfig.getCodeLength() + "d", code);
	}

	private String getFormattedPhoneNumber(String number) throws InvalidPhoneNumberException {

		if (number == null) {
			throw new InvalidPhoneNumberException("The phone number is null");
		}

		PhoneNumber phoneNumber;

		if (number.equals(smsAuthConfig.getEasterEggPhoneNumber())) {
			return number;
		}

		try {
			phoneNumber = phoneNumberUtil.parse(number, smsAuthConfig.getRegion());
		} catch (NumberParseException e) {
			throw new InvalidPhoneNumberException("Failed to parse the phone number: " + number);
		}
		if (!phoneNumberUtil.isValidNumber(phoneNumber)) {
			throw new InvalidPhoneNumberException("The phone number is not valid: " + number);
		}
		if (!smsAuthConfig.getAllowedRegion().isEmpty()
				&& !smsAuthConfig.getAllowedRegion().contains(phoneNumber.getCountryCode())) {
			throw new InvalidPhoneNumberException(
					"The phone number " + number + " does not belong to the allowed regions.");
		}
		return phoneNumberUtil.format(phoneNumber, PhoneNumberFormat.E164);
	}

	@Transactional
	private boolean lockPhoneNumber(String phoneNumber) {
		PhonenumberLock lock = phonenumberLockRepository.findById(phoneNumber).orElse(new PhonenumberLock(phoneNumber));
		if (lock.getLocked())
			return false;
		lock.setLocked(true);
		try {
			phonenumberLockRepository.saveAndFlush(lock);
		} catch (DataIntegrityViolationException e) {
			return false;
		}
		return true;
	}

	@Transactional
	private void releasePhoneNumber(String phoneNumber) {
		PhonenumberLock lock = phonenumberLockRepository.findById(phoneNumber)
				.orElseThrow(EntityNotFoundException::new);
		lock.setLocked(false);
		phonenumberLockRepository.saveAndFlush(lock);
	}

	public void sendResetSms(String number) throws InvalidPhoneNumberException, TooManySmsSentException {
		sendSms(number, "%s", SmsCodeType.RESET);
	}

	public void sendResetSms(String number, String verificationTemplate)
			throws InvalidPhoneNumberException, TooManySmsSentException {
		sendSms(number, verificationTemplate, SmsCodeType.RESET);
	}

	public void verifyResetSms(String number, String uuid, String code, String warningMessage)
			throws InvalidCodeException, InvalidPhoneNumberException {

		number = getFormattedPhoneNumber(number);

		validateSmsCode(number, code, SmsCodeType.RESET);
		if (smsAuthConfig.getTransferDelayInHours() > 0) {
			smsSenderService.sendSms(number, warningMessage);
		}
		PhoneUuid phoneUuid = phoneUuidRepository.findById(number).orElseThrow(InvalidCodeException::new);
		phoneUuid.setNewUuid(uuid);
		phoneUuid.setChangeRequestedAt(Instant.now());
		phoneUuidRepository.save(phoneUuid);
	}

	private boolean verifyUuid(PhoneUuid phoneUuid, String uuid) {
		if (phoneUuid == null || phoneUuid.getUuid().equals(uuid)) {
			return true;
		}
		if (phoneUuid.getNewUuid() == null || phoneUuid.getChangeRequestedAt() == null) {
			return false;
		}
		if (phoneUuid.getNewUuid().equals(uuid)
				&& phoneUuid.getChangeRequestedAt().plus(Duration.ofHours(smsAuthConfig.getTransferDelayInHours())).isBefore(Instant.now())) {
			return true;
		}
		return false;
	}

	private void updateUuid(PhoneUuid phoneUuid, String uuid) {

		phoneUuid.setUuid(uuid);
		phoneUuid.setNewUuid(null);
		phoneUuid.setChangeRequestedAt(null);
		phoneUuidRepository.save(phoneUuid);
	}

	public void sendSmsSecure(String number, String uuid) throws InvalidPhoneNumberException, TooManySmsSentException {
		sendSmsSecure(number, uuid, "%s");
	}

	public void sendSmsSecure(String number, String uuid, String messageTemplate)
			throws InvalidPhoneNumberException, TooManySmsSentException {

		number = getFormattedPhoneNumber(number);

		PhoneUuid phoneUuid = phoneUuidRepository.findById(number).orElse(null);

		if (!verifyUuid(phoneUuid, uuid)) {
			LOGGER.error(
					"The phone uuid does not matched the registered one. Potential attempt to steal an account. SMS not sent.");
			return;
		}
		sendSms(number, messageTemplate, SmsCodeType.VALIDATION);
	}

	private void sendSms(String number, String messageTemplate, SmsCodeType type)
			throws InvalidPhoneNumberException, TooManySmsSentException {
		if (number != null && number.equals(smsAuthConfig.getEasterEggPhoneNumber())) {
			return;
		}
		number = getFormattedPhoneNumber(number);

		if (!lockPhoneNumber(number)) {
			throw new TooManySmsSentException();
		}
		try {
			Instant oldest = Instant.now().minus(Duration.ofMinutes(smsAuthConfig.getPeriodInMinutes()));
			List<SmsCode> smsCodes = smsCodeRepository
					.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(number, oldest);
			long nbUnvalidated = smsCodes.stream().filter(smsCode -> smsCode.getValidated()).count();
			if (nbUnvalidated >= smsAuthConfig.getMaxSmsPerPeriod()) {
				throw new TooManySmsSentException();
			}
			if (!smsCodes.isEmpty()) {
				SmsCode previousCode = smsCodes.get(0);
				smsCodeRepository.findByPhonenumberAndCode(number, previousCode.getCode());
				if (previousCode.getCreatedAt()
						.plus(Duration.ofSeconds(smsAuthConfig.getMinTimeBetweenTwoSmsInSecond()))
						.compareTo(Instant.now()) > 0) {
					throw new TooManySmsSentException();
				}
			}
			String code = generateCode();
			String content = String.format(messageTemplate, code);

			smsSenderService.sendSms(number, content);

			SmsCode smsCode = new SmsCode();
			SmsCodeId id = new SmsCodeId();
			id.setCode(code);
			id.setPhonenumber(number);
			smsCode.setCode(code);
			smsCode.setPhonenumber(number);
			smsCode.setId(id);
			smsCode.setType(type);
			smsCode.setCreatedAt(Instant.now());
			smsCodeRepository.save(smsCode);
		} finally {
			releasePhoneNumber(number);
		}
	}

	public void validateSmsCodeSecure(String phoneNumber, String uuid, String code)
			throws InvalidCodeException, InvalidPhoneNumberException {

		phoneNumber = getFormattedPhoneNumber(phoneNumber);

		PhoneUuid phoneUuid = phoneUuidRepository.findById(phoneNumber).orElse(null);
		if (!verifyUuid(phoneUuid, uuid)) {
			LOGGER.error(
					"The phone uuid does not matched the registered one. Potential attempt to steal an account. SMS verification blocked.");
			throw new InvalidCodeException();
		}
		validateSmsCode(phoneNumber, code, SmsCodeType.VALIDATION);
		if (phoneUuid == null) {
			phoneUuid = new PhoneUuid();
			phoneUuid.setPhoneNumber(phoneNumber);
			LOGGER.info("Pairing the phone number '" + phoneNumber + "' with the uuid: '" + uuid + "'.");
		}
		updateUuid(phoneUuid, uuid);
	}

	public void validateSmsCode(String phoneNumber, String code, SmsCodeType type)
			throws InvalidCodeException, InvalidPhoneNumberException {
		if (phoneNumber.equals(smsAuthConfig.getEasterEggPhoneNumber())
				&& code.equals(smsAuthConfig.getEasterEggCode())) {
			return;
		}
		phoneNumber = getFormattedPhoneNumber(phoneNumber);

		if (!lockPhoneNumber(phoneNumber)) {
			throw new TooManyTrialsException();
		}
		try {
			Instant oldest = Instant.now().minus(Duration.ofMinutes(smsAuthConfig.getCodeValidityInMinutes()));
			List<SmsCode> smsCodes = smsCodeRepository
					.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(phoneNumber, oldest);
			if (smsCodes.isEmpty()) {
				throw new ExpiredCodeException();
			}
			SmsCode smsCode = smsCodes.get(0);
			if (smsCode.getValidated() || !smsCode.getType().equals(type)) {
				throw new ExpiredCodeException();
			}
			if (smsCode.getTrials() >= smsAuthConfig.getMaxTrialsPerCode()) {
				throw new TooManyTrialsException();
			}
			if (!smsCode.getCode().equals(code)) {
				smsCode.incrementTrials();
				smsCodeRepository.save(smsCode);
				throw new WrongCodeException();
			}
			smsCode.setValidated(true);
			smsCodeRepository.save(smsCode);
		} finally {
			releasePhoneNumber(phoneNumber);
		}
	}

	public void verifyValidationSmsCode(String phoneNumber, String code)
			throws InvalidCodeException, InvalidPhoneNumberException {
		validateSmsCode(phoneNumber, code, SmsCodeType.VALIDATION);
	}

	public void sendValidationSms(String phoneNumber) throws InvalidPhoneNumberException, TooManySmsSentException {
		sendValidationSms(phoneNumber, "%s");
	}

	public void sendValidationSms(String phoneNumber, String smsVerificationTemplate)
			throws InvalidPhoneNumberException, TooManySmsSentException {
		sendSms(phoneNumber, smsVerificationTemplate, SmsCodeType.VALIDATION);
	}

	/*
	 * Send a login SMS if the Device UUID is new or valid. Send a reset SMS if the
	 * Device UUID differs from the stored one.
	 */
	public SmsCodeType sendLoginOrResetSms(String phoneNumber, String deviceUuid, String loginTemplate,
			String ResetTemplate) throws InvalidPhoneNumberException, TooManySmsSentException {

		phoneNumber = getFormattedPhoneNumber(phoneNumber);

		PhoneUuid phoneUuid = phoneUuidRepository.findById(phoneNumber).orElse(null);
		if (verifyUuid(phoneUuid, deviceUuid)) {
			this.sendSmsSecure(phoneNumber, deviceUuid, loginTemplate);
			return SmsCodeType.VALIDATION;
		}
		this.sendResetSms(phoneNumber, ResetTemplate);
		return SmsCodeType.RESET;
	}

	/*
	 * Verify the code provided. If the deviceUuid is different to the registered
	 * one, this will trigger a reset process.
	 */
	public SmsCodeType verifyLoginOrResetSms(String phoneNumber, String deviceUuid, String code, String warningMessage)
			throws InvalidCodeException, InvalidPhoneNumberException {

		phoneNumber = getFormattedPhoneNumber(phoneNumber);

		PhoneUuid phoneUuid = phoneUuidRepository.findById(phoneNumber).orElse(null);
		if (verifyUuid(phoneUuid, deviceUuid)) {
			this.validateSmsCodeSecure(phoneNumber, deviceUuid, code);
			return SmsCodeType.VALIDATION;
		}
		this.verifyResetSms(phoneNumber, deviceUuid, code, warningMessage);
		return SmsCodeType.RESET;
	}

	public void cancelUuidUpdate(String phoneNumber) throws InvalidPhoneNumberException {

		phoneNumber = getFormattedPhoneNumber(phoneNumber);

		PhoneUuid phoneUuid = phoneUuidRepository.findById(phoneNumber).orElse(null);
		phoneUuid.setNewUuid(null);
		phoneUuid.setChangeRequestedAt(null);
		phoneUuidRepository.save(phoneUuid);
	}
}
