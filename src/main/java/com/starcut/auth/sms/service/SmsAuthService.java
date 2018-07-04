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
		PhoneNumber phoneNumber;
		try {
			phoneNumber = phoneNumberUtil.parse(number, smsAuthConfig.getRegion());
		} catch (NumberParseException e) {
			throw new InvalidPhoneNumberException();
		}
		if (!phoneNumberUtil.isValidNumber(phoneNumber)) {
			throw new InvalidPhoneNumberException();
		}
		if (!smsAuthConfig.getAllowedRegion().isEmpty()
				&& !smsAuthConfig.getAllowedRegion().contains(phoneNumber.getCountryCode())) {
			throw new InvalidPhoneNumberException();
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

	public void sendResetSms(String number, String verificationTemplate)
			throws InvalidPhoneNumberException, TooManySmsSentException {
		sendSms(number, verificationTemplate);
	}

	public void verifyResetSms(String number, String uuid, String code, String warningMessage)
			throws InvalidCodeException, InvalidPhoneNumberException {
		validateSmsCode(number, code);
		smsSenderService.sendSms(number, warningMessage);
		PhoneUuid phoneUuid = phoneUuidRepository.findById(number).orElseThrow(InvalidCodeException::new);
		phoneUuid.setNewUuid(uuid);
	}

	private boolean verifyUuid(PhoneUuid phoneUuid, String uuid) {
		if (phoneUuid == null || phoneUuid.getUuid().equals(uuid)) {
			return true;
		}
		if (phoneUuid.getNewUuid() == null || phoneUuid.getChangeRequestedAt() == null) {
			return false;
		}
		if (phoneUuid.getNewUuid().equals(uuid) && phoneUuid.getChangeRequestedAt()
				.plus(Duration.ofHours(smsAuthConfig.getCodeValidityInMinutes())).isAfter(Instant.now())) {
			return true;
		}
		return false;
	}

	private void updateUuid(PhoneUuid phoneUuid, String uuid) {
		phoneUuid.setUuid(uuid);
		phoneUuid.setNewUuid(null);
		phoneUuid.setChangeRequestedAt(null);
	}

	public void sendSmsSecure(String number, String uuid) throws InvalidPhoneNumberException, TooManySmsSentException {
		sendSmsSecure(number, uuid, "%s");
	}

	public void sendSmsSecure(String number, String uuid, String messageTemplate)
			throws InvalidPhoneNumberException, TooManySmsSentException {
		PhoneUuid phoneUuid = phoneUuidRepository.findById(number).orElse(null);

		if (!verifyUuid(phoneUuid, uuid)) {
			LOGGER.error(
					"The phone uuid does not matched the registered one. Potential attempt to steal an account. SMS not sent.");
			return;
		}
		sendSms(number, messageTemplate);
	}

	public void sendSms(String number) throws InvalidPhoneNumberException, TooManySmsSentException {
		sendSms(number, "%s");
	}

	public void sendSms(String number, String messageTemplate)
			throws InvalidPhoneNumberException, TooManySmsSentException {
		String formattedPhoneNumber = getFormattedPhoneNumber(number);
		if (!lockPhoneNumber(formattedPhoneNumber)) {
			throw new TooManySmsSentException();
		}
		try {
			Instant oldest = Instant.now().minus(Duration.ofMinutes(smsAuthConfig.getPeriodInMinutes()));
			List<SmsCode> smsCodes = smsCodeRepository
					.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(formattedPhoneNumber, oldest);
			long nbUnvalidated = smsCodes.stream().filter(smsCode -> smsCode.getValidated()).count();
			if (nbUnvalidated >= smsAuthConfig.getMaxSmsPerPeriod()) {
				throw new TooManySmsSentException();
			}
			if (!smsCodes.isEmpty()) {
				SmsCode previousCode = smsCodes.get(0);
				smsCodeRepository.findByPhonenumberAndCode(formattedPhoneNumber, previousCode.getCode());
				if (previousCode.getCreatedAt()
						.plus(Duration.ofSeconds(smsAuthConfig.getMinTimeBetweenTwoSmsInSecond()))
						.compareTo(Instant.now()) > 0) {
					throw new TooManySmsSentException();
				}
			}
			String code = generateCode();
			String content = String.format(messageTemplate, code);

			smsSenderService.sendSms(formattedPhoneNumber, content);

			SmsCode smsCode = new SmsCode();
			SmsCodeId id = new SmsCodeId();
			id.setCode(code);
			id.setPhonenumber(formattedPhoneNumber);
			smsCode.setCode(code);
			smsCode.setPhonenumber(formattedPhoneNumber);
			smsCode.setId(id);
			smsCodes.add(smsCode);
			smsCodeRepository.save(smsCode);
		} finally {
			releasePhoneNumber(formattedPhoneNumber);
		}
	}

	public void validateSmsCodeSecure(String phoneNumber, String uuid, String code)
			throws InvalidCodeException, InvalidPhoneNumberException {

		PhoneUuid phoneUuid = phoneUuidRepository.findById(phoneNumber).orElse(null);
		validateSmsCode(phoneNumber, code);

		if (!verifyUuid(phoneUuid, uuid)) {
			LOGGER.error(
					"The phone uuid does not matched the registered one. Potential attempt to steal an account. SMS verification blocked.");
			throw new InvalidCodeException();
		}
		updateUuid(phoneUuid, uuid);

		if (phoneUuid == null) {
			phoneUuid = new PhoneUuid();
			phoneUuid.setPhoneNumber(phoneNumber);
			phoneUuid.setUuid(uuid);
			LOGGER.info("Pairing the phone number '" + phoneNumber + "' with the uuid: '" + uuid + "'.");
			phoneUuidRepository.save(phoneUuid);
		}
	}

	public void validateSmsCode(String phonenumber, String code)
			throws InvalidCodeException, InvalidPhoneNumberException {
		String formattedPhoneNumber = getFormattedPhoneNumber(phonenumber);
		if (!lockPhoneNumber(formattedPhoneNumber)) {
			throw new TooManyTrialsException();
		}
		try {
			Instant oldest = Instant.now().minus(Duration.ofMinutes(smsAuthConfig.getCodeValidityInMinutes()));
			List<SmsCode> smsCodes = smsCodeRepository
					.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(formattedPhoneNumber, oldest);
			if (smsCodes.isEmpty()) {
				throw new ExpiredCodeException();
			}
			SmsCode smsCode = smsCodes.get(0);
			if (smsCode.getValidated()) {
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
			releasePhoneNumber(formattedPhoneNumber);
		}
	}

}
