package com.starcut.auth.sms;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.starcut.auth.sms.db.SmsCodeRepository;
import com.starcut.auth.sms.db.entity.SmsCode;
import com.starcut.auth.sms.db.entity.type.SmsCodeType;
import com.starcut.auth.sms.exceptions.InvalidCodeException;
import com.starcut.auth.sms.exceptions.InvalidPhoneNumberException;
import com.starcut.auth.sms.exceptions.TooManySmsSentException;
import com.starcut.auth.sms.service.SmsAuthService;
import com.starcut.auth.sms.service.SmsSenderService;

public class UnformattedPhoneNumberTest extends SmsAuthSpringBootStarterApplicationTests {

	@Autowired
	private SmsCodeRepository smsCodeRepository;

	@MockBean
	private SmsSenderService smsSenderService;

	@InjectMocks
	@Autowired
	private SmsAuthService smsAuthService;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void changingPhoneWithUnformattedNumber()
			throws InvalidPhoneNumberException, TooManySmsSentException, InvalidCodeException, InterruptedException {
		String phoneNumber = "0477777777";
		String formattedPhoneNumber = "+358477777777";
		String uuid1 = "NaN";
		String uuid2 = "other";

		SmsCodeType smsCodeType = smsAuthService.sendLoginOrResetSms(phoneNumber, uuid1, "%s", "%s");
		assertEquals(SmsCodeType.VALIDATION, smsCodeType);

		SmsCode lastSmsCode = smsCodeRepository
				.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(formattedPhoneNumber, Instant.MIN)
				.get(0);

		smsAuthService.verifyLoginOrResetSms(phoneNumber, uuid1, lastSmsCode.getCode(), "");

		lastSmsCode.setCreatedAt(Instant.now().minus(Duration.of(120, ChronoUnit.DAYS)));
		smsCodeRepository.save(lastSmsCode);

		smsCodeType = smsAuthService.sendLoginOrResetSms(formattedPhoneNumber, uuid2, "%s", "%s");
		assertEquals(SmsCodeType.RESET, smsCodeType);
	}
}
