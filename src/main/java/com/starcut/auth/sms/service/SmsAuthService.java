//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.starcut.auth.sms.config.SmsAuthConfig;
import com.starcut.auth.sms.db.PhoneUuidRepository;
import com.starcut.auth.sms.db.PhonenumberLockRepository;
import com.starcut.auth.sms.db.SmsCodeRepository;
import com.starcut.auth.sms.db.SmsLogRepository;
import com.starcut.auth.sms.db.entity.PhoneUuid;
import com.starcut.auth.sms.db.entity.PhonenumberLock;
import com.starcut.auth.sms.db.entity.SmsCode;
import com.starcut.auth.sms.db.entity.SmsCodeId;
import com.starcut.auth.sms.db.entity.SmsLog;
import com.starcut.auth.sms.db.entity.type.SmsCodeType;
import com.starcut.auth.sms.exceptions.ExpiredCodeException;
import com.starcut.auth.sms.exceptions.InvalidCodeException;
import com.starcut.auth.sms.exceptions.InvalidPhoneNumberException;
import com.starcut.auth.sms.exceptions.TooManySmsSentException;
import com.starcut.auth.sms.exceptions.TooManyTrialsException;
import com.starcut.auth.sms.exceptions.WrongCodeException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class SmsAuthService {
    @Autowired
    private SmsCodeRepository smsCodeRepository;
    @Autowired
    private PhonenumberLockRepository phonenumberLockRepository;
    @Autowired
    private PhoneUuidRepository phoneUuidRepository;
    @Autowired
    private SmsLogRepository smsLogRepository;
    @Autowired
    private SmsSenderService smsSenderService;
    private SmsAuthConfig smsAuthConfig;
    private SecureRandom secureRandom = new SecureRandom();
    private PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public SmsAuthService(SmsAuthConfig authSmsConfig) {
        this.smsAuthConfig = authSmsConfig;
    }

    private String generateCode() {
        Long numberOfCodes = (long)Math.pow(10.0D, (double)this.smsAuthConfig.getCodeLength());
        Long code = Math.abs(this.secureRandom.nextLong()) % numberOfCodes;
        return String.format("%0" + this.smsAuthConfig.getCodeLength() + "d", code);
    }

    private String getFormattedPhoneNumber(String number) throws InvalidPhoneNumberException {
        if (number == null) {
            throw new InvalidPhoneNumberException("The phone number is null");
        } else if (number.equals(this.smsAuthConfig.getEasterEggPhoneNumber())) {
            return number;
        } else {
            PhoneNumber phoneNumber;
            try {
                phoneNumber = this.phoneNumberUtil.parse(number, this.smsAuthConfig.getRegion());
            } catch (NumberParseException var4) {
                throw new InvalidPhoneNumberException("Failed to parse the phone number: " + number);
            }

            if (!this.phoneNumberUtil.isValidNumber(phoneNumber)) {
                throw new InvalidPhoneNumberException("The phone number is not valid: " + number);
            } else if (!this.smsAuthConfig.getAllowedRegion().isEmpty() && !this.smsAuthConfig.getAllowedRegion().contains(phoneNumber.getCountryCode())) {
                throw new InvalidPhoneNumberException("The phone number " + number + " does not belong to the allowed regions.");
            } else {
                return this.phoneNumberUtil.format(phoneNumber, PhoneNumberFormat.E164);
            }
        }
    }

    @Transactional
    private boolean lockPhoneNumber(String phoneNumber) {
        PhonenumberLock lock = (PhonenumberLock)this.phonenumberLockRepository.findById(phoneNumber).orElse(new PhonenumberLock(phoneNumber));
        if (lock.getLocked()) {
            return false;
        } else {
            lock.setLocked(true);

            try {
                this.phonenumberLockRepository.saveAndFlush(lock);
                return true;
            } catch (DataIntegrityViolationException var4) {
                return false;
            }
        }
    }

    @Transactional
    private void releasePhoneNumber(String phoneNumber) {
        PhonenumberLock lock = (PhonenumberLock)this.phonenumberLockRepository.findById(phoneNumber).orElseThrow(EntityNotFoundException::new);
        lock.setLocked(false);
        this.phonenumberLockRepository.saveAndFlush(lock);
    }

    public void sendResetSms(String number) throws InvalidPhoneNumberException, TooManySmsSentException {
        this.sendSms(number, "%s", SmsCodeType.RESET);
    }

    public void sendResetSms(String number, String verificationTemplate) throws InvalidPhoneNumberException, TooManySmsSentException {
        this.sendSms(number, verificationTemplate, SmsCodeType.RESET);
    }

    /** @deprecated */
    @Deprecated
    public void verifyResetSms(String number, String uuid, String code, String warningMessage) throws InvalidCodeException, InvalidPhoneNumberException {
        number = this.getFormattedPhoneNumber(number);
        this.validateSmsCode(number, code, SmsCodeType.RESET);
        if (this.smsAuthConfig.getTransferDelayInHours() > 0) {
            this.smsSenderService.sendSms(number, warningMessage, (String)null);
        }

        PhoneUuid phoneUuid = (PhoneUuid)this.phoneUuidRepository.findById(number).orElseThrow(InvalidCodeException::new);
        phoneUuid.setNewUuid(uuid);
        phoneUuid.setChangeRequestedAt(Instant.now());
        this.phoneUuidRepository.save(phoneUuid);
    }

    private boolean verifyUuid(PhoneUuid phoneUuid, String uuid) {
        if (phoneUuid != null && !phoneUuid.getUuid().equals(uuid)) {
            if (phoneUuid.getNewUuid() != null && phoneUuid.getChangeRequestedAt() != null) {
                return phoneUuid.getNewUuid().equals(uuid) && phoneUuid.getChangeRequestedAt().plus(Duration.ofHours((long)this.smsAuthConfig.getTransferDelayInHours())).isBefore(Instant.now());
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void updateUuid(PhoneUuid phoneUuid, String uuid) {
        phoneUuid.setUuid(uuid);
        phoneUuid.setNewUuid((String)null);
        phoneUuid.setChangeRequestedAt((Instant)null);
        this.phoneUuidRepository.save(phoneUuid);
    }

    public void sendSmsSecure(String number, String uuid) throws InvalidPhoneNumberException, TooManySmsSentException {
        this.sendSmsSecure(number, uuid, "%s");
    }

    public void sendSmsSecure(String number, String uuid, String messageTemplate) throws InvalidPhoneNumberException, TooManySmsSentException {
        number = this.getFormattedPhoneNumber(number);
        PhoneUuid phoneUuid = (PhoneUuid)this.phoneUuidRepository.findById(number).orElse((PhoneUuid)null);
        if (!this.verifyUuid(phoneUuid, uuid)) {
            this.LOGGER.error("The phone uuid does not matched the registered one. Potential attempt to steal an account. SMS not sent.");
        } else {
            this.sendSms(number, messageTemplate, SmsCodeType.VALIDATION);
        }
    }

    private String sendSms(String number, String messageTemplate, SmsCodeType type) throws InvalidPhoneNumberException, TooManySmsSentException {
        return this.sendSms(number, messageTemplate, type, this.smsAuthConfig.getSenderId(), (String)null);
    }

    private String sendSms(String number, String messageTemplate, SmsCodeType type, String senderId, String teamUUID) throws InvalidPhoneNumberException, TooManySmsSentException {
        String requestId = null;
        number = this.getFormattedPhoneNumber(number);
        if (!this.lockPhoneNumber(number)) {
            throw new TooManySmsSentException();
        } else {
            String var16;
            try {
                Instant oldest = Instant.now().minus(Duration.ofMinutes((long)this.smsAuthConfig.getPeriodInMinutes()));
                List<SmsCode> smsCodes = this.smsCodeRepository.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(number, oldest);
                long nbUnvalidated = smsCodes.stream().filter((smsCodex) -> {
                    return smsCodex.getValidated();
                }).count();
                if (nbUnvalidated >= (long)this.smsAuthConfig.getMaxSmsPerPeriod()) {
                    throw new TooManySmsSentException();
                }

                if (!smsCodes.isEmpty()) {
                    SmsCode previousCode = (SmsCode)smsCodes.get(0);
                    this.smsCodeRepository.findByPhonenumberAndCode(number, previousCode.getCode());
                    if (previousCode.getCreatedAt().plus(Duration.ofSeconds((long)this.smsAuthConfig.getMinTimeBetweenTwoSmsInSecond())).compareTo(Instant.now()) > 0) {
                        throw new TooManySmsSentException();
                    }
                }

                String code = this.generateCode();
                String content = String.format(messageTemplate, code);
                if (!number.equals(this.smsAuthConfig.getEasterEggPhoneNumber())) {
                    requestId = this.smsSenderService.sendSms(number, content, senderId, teamUUID);
                    SmsCode smsCode = new SmsCode();
                    SmsCodeId id = new SmsCodeId();
                    id.setCode(code);
                    id.setPhonenumber(number);
                    smsCode.setCode(code);
                    smsCode.setPhonenumber(number);
                    smsCode.setId(id);
                    smsCode.setType(type);
                    smsCode.setCreatedAt(Instant.now());
                    this.smsCodeRepository.save(smsCode);
                    return requestId;
                }

                code = this.smsAuthConfig.getEasterEggCode();
                requestId = UUID.randomUUID().toString();
                SmsLog smsLog = new SmsLog();
                smsLog.setMessage(content);
                smsLog.setRequestId(requestId);
                smsLog.setPhoneNumber(number);
                smsLog.setSenderId(senderId);
                smsLog.setTeamUUID(teamUUID);
                this.smsLogRepository.saveAndFlush(smsLog);
                var16 = requestId;
            } finally {
                this.releasePhoneNumber(number);
            }

            return var16;
        }
    }

    public void validateSmsCodeSecure(String phoneNumber, String uuid, String code) throws InvalidCodeException, InvalidPhoneNumberException {
        phoneNumber = this.getFormattedPhoneNumber(phoneNumber);
        PhoneUuid phoneUuid = (PhoneUuid)this.phoneUuidRepository.findById(phoneNumber).orElse((PhoneUuid)null);
        if (!this.verifyUuid(phoneUuid, uuid)) {
            this.LOGGER.error("The phone uuid does not matched the registered one. Potential attempt to steal an account. SMS verification blocked.");
            throw new InvalidCodeException();
        } else {
            this.validateSmsCode(phoneNumber, code, SmsCodeType.VALIDATION);
            if (phoneUuid == null) {
                phoneUuid = new PhoneUuid();
                phoneUuid.setPhoneNumber(phoneNumber);
                this.LOGGER.info("Pairing the phone number '" + phoneNumber + "' with the uuid: '" + uuid + "'.");
            }

            this.updateUuid(phoneUuid, uuid);
        }
    }

    public void validateSmsCode(String phoneNumber, String code, SmsCodeType type) throws InvalidCodeException, InvalidPhoneNumberException {
        if (!phoneNumber.equals(this.smsAuthConfig.getEasterEggPhoneNumber()) || !code.equals(this.smsAuthConfig.getEasterEggCode())) {
            phoneNumber = this.getFormattedPhoneNumber(phoneNumber);
            if (!this.lockPhoneNumber(phoneNumber)) {
                throw new TooManyTrialsException();
            } else {
                try {
                    Instant oldest = Instant.now().minus(Duration.ofMinutes((long)this.smsAuthConfig.getCodeValidityInMinutes()));
                    List<SmsCode> smsCodes = this.smsCodeRepository.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(phoneNumber, oldest);
                    if (smsCodes.isEmpty()) {
                        throw new ExpiredCodeException();
                    }

                    SmsCode smsCode = (SmsCode)smsCodes.get(0);
                    if (smsCode.getValidated() || !smsCode.getType().equals(type)) {
                        throw new ExpiredCodeException();
                    }

                    if (smsCode.getTrials() >= this.smsAuthConfig.getMaxTrialsPerCode()) {
                        throw new TooManyTrialsException();
                    }

                    if (!smsCode.getCode().equals(code)) {
                        smsCode.incrementTrials();
                        this.smsCodeRepository.save(smsCode);
                        throw new WrongCodeException();
                    }

                    smsCode.setValidated(true);
                    this.smsCodeRepository.save(smsCode);
                } finally {
                    this.releasePhoneNumber(phoneNumber);
                }

            }
        }
    }

    public void verifyValidationSmsCode(String phoneNumber, String code) throws InvalidCodeException, InvalidPhoneNumberException {
        this.validateSmsCode(phoneNumber, code, SmsCodeType.VALIDATION);
    }

    public String sendValidationSms(String phoneNumber) throws InvalidPhoneNumberException, TooManySmsSentException {
        return this.sendValidationSms(phoneNumber, "%s");
    }

    public String sendValidationSms(String phoneNumber, String smsVerificationTemplate) throws InvalidPhoneNumberException, TooManySmsSentException {
        return this.sendSms(phoneNumber, smsVerificationTemplate, SmsCodeType.VALIDATION);
    }

    /** @deprecated */
    @Deprecated
    public SmsCodeType sendLoginOrResetSms(String phoneNumber, String deviceUuid, String loginTemplate, String ResetTemplate) throws InvalidPhoneNumberException, TooManySmsSentException {
        phoneNumber = this.getFormattedPhoneNumber(phoneNumber);
        PhoneUuid phoneUuid = (PhoneUuid)this.phoneUuidRepository.findById(phoneNumber).orElse((PhoneUuid)null);
        if (this.verifyUuid(phoneUuid, deviceUuid)) {
            this.sendSmsSecure(phoneNumber, deviceUuid, loginTemplate);
            return SmsCodeType.VALIDATION;
        } else {
            this.sendResetSms(phoneNumber, ResetTemplate);
            return SmsCodeType.RESET;
        }
    }

    /** @deprecated */
    @Deprecated
    public SmsCodeType verifyLoginOrResetSms(String phoneNumber, String deviceUuid, String code, String warningMessage) throws InvalidCodeException, InvalidPhoneNumberException {
        phoneNumber = this.getFormattedPhoneNumber(phoneNumber);
        PhoneUuid phoneUuid = (PhoneUuid)this.phoneUuidRepository.findById(phoneNumber).orElse((PhoneUuid)null);
        if (this.verifyUuid(phoneUuid, deviceUuid)) {
            this.validateSmsCodeSecure(phoneNumber, deviceUuid, code);
            return SmsCodeType.VALIDATION;
        } else {
            this.verifyResetSms(phoneNumber, deviceUuid, code, warningMessage);
            return SmsCodeType.RESET;
        }
    }

    /** @deprecated */
    @Deprecated
    public void cancelUuidUpdate(String phoneNumber) throws InvalidPhoneNumberException {
        phoneNumber = this.getFormattedPhoneNumber(phoneNumber);
        PhoneUuid phoneUuid = (PhoneUuid)this.phoneUuidRepository.findById(phoneNumber).orElse((PhoneUuid)null);
        phoneUuid.setNewUuid((String)null);
        phoneUuid.setChangeRequestedAt((Instant)null);
        this.phoneUuidRepository.save(phoneUuid);
    }

    public String verifyValidationSmsCodeByRequestId(String requestId, String code) throws InvalidCodeException, InvalidPhoneNumberException {
        SmsLog smsLog = (SmsLog)this.smsLogRepository.findByRequestId(requestId).orElseThrow(InvalidCodeException::new);
        String phoneNumber = smsLog.getPhoneNumber();
        this.verifyValidationSmsCode(phoneNumber, code);
        return phoneNumber;
    }

    public String sendValidationSmsWithSenderId(String phoneNumber, String smsVerificationTemplate, String senderId) throws InvalidPhoneNumberException, TooManySmsSentException {
        return this.sendSms(phoneNumber, smsVerificationTemplate, SmsCodeType.VALIDATION, senderId, (String)null);
    }

    public String sendValidationSmsWithSenderId(String phoneNumber, String smsVerificationTemplate, String senderId, String teamUUID) throws InvalidPhoneNumberException, TooManySmsSentException {
        return this.sendSms(phoneNumber, smsVerificationTemplate, SmsCodeType.VALIDATION, senderId, teamUUID);
    }
}
