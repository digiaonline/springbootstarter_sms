package com.starcut.auth.sms.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.starcut.auth.sms.config.SmsAuthConfig;
import com.starcut.auth.sms.db.SmsCode;
import com.starcut.auth.sms.db.SmsCodeId;
import com.starcut.auth.sms.db.SmsCodeRepository;
import com.starcut.auth.sms.exceptions.ExpiredCodeException;
import com.starcut.auth.sms.exceptions.InvalidCodeException;
import com.starcut.auth.sms.exceptions.TooManySmsSentException;
import com.starcut.auth.sms.exceptions.TooManyTrialsException;
import com.starcut.auth.sms.exceptions.WrongCodeException;

@Service
public class SmsAuthService {

	@Autowired
	private SmsCodeRepository smsCodeRepository;

	private SmsAuthConfig smsAuthConfig;

	private SecureRandom secureRandom = new SecureRandom();

	public SmsAuthService(SmsAuthConfig authSmsConfig) {
		this.smsAuthConfig = authSmsConfig;
	}

	private String generateCode() {
		Long numberOfCodes = (long) Math.pow(10, smsAuthConfig.getCodeLength());
		Long code = Math.abs(secureRandom.nextLong()) % numberOfCodes;
		return String.format("%0" + smsAuthConfig.getCodeLength() + "d", code);
	}

	public void sendSms(String phonenumber) throws TooManySmsSentException {
		Instant oldest = Instant.now().minus(Duration.ofMinutes(smsAuthConfig.getPeriodInMinutes()));
		List<SmsCode> smsCodes = smsCodeRepository
				.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(phonenumber, oldest);
		if (smsCodes.size() >= smsAuthConfig.getMaxSmsPerPeriod()) {
			throw new TooManySmsSentException();
		}
		String code = generateCode();
		SmsCode smsCode = new SmsCode();
		SmsCodeId id = new SmsCodeId();
		id.setCode(code);
		id.setPhonenumber(phonenumber);
		smsCode.setCode(code);
		smsCode.setPhonenumber(phonenumber);
		smsCode.setId(id);
		smsCodeRepository.save(smsCode);
	}

	public void validateSmsCode(String phonenumber, String code) throws InvalidCodeException {
		Instant oldest = Instant.now().minus(Duration.ofMinutes(smsAuthConfig.getCodeValidityInMinutes()));
		List<SmsCode> smsCodes = smsCodeRepository
				.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(phonenumber, oldest);
		if (smsCodes.isEmpty()) {
			throw new ExpiredCodeException();
		}
		SmsCode smsCode = smsCodes.get(0);
		if (smsCode.getTrials() >= smsAuthConfig.getMaxTrialsPerCode()) {
			throw new TooManyTrialsException();
		}
		if (!smsCode.getCode().equals(code)) {
			smsCode.incrementTrials();
			smsCodeRepository.save(smsCode);
			throw new WrongCodeException();
		}
		smsCodeRepository.delete(smsCode);
	}

}
