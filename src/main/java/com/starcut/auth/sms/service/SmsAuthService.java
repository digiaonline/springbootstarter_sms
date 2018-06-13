package com.starcut.auth.sms.service;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.starcut.auth.sms.config.SmsAuthConfig;
import com.starcut.auth.sms.db.SmsCode;
import com.starcut.auth.sms.db.SmsCodeId;
import com.starcut.auth.sms.db.SmsCodeRepository;

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

	public void sendSms(String phonenumber) {
		// TODO throw exception
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
	
}
