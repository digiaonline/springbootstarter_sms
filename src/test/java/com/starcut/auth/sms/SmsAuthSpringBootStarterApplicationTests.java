package com.starcut.auth.sms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.starcut.auth.sms.config.SmsAuthConfig;
import com.starcut.auth.sms.db.SmsCode;
import com.starcut.auth.sms.db.SmsCodeRepository;
import com.starcut.auth.sms.exceptions.ExpiredCodeException;
import com.starcut.auth.sms.exceptions.InvalidCodeException;
import com.starcut.auth.sms.exceptions.InvalidPhoneNumberException;
import com.starcut.auth.sms.exceptions.SmsAuthException;
import com.starcut.auth.sms.exceptions.TooManySmsSentException;
import com.starcut.auth.sms.exceptions.TooManyTrialsException;
import com.starcut.auth.sms.exceptions.WrongCodeException;
import com.starcut.auth.sms.service.SmsAuthService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestConfiguration(value = "com.starcut.auth.sms.config.SmsAuthAutoConfiguration")
public class SmsAuthSpringBootStarterApplicationTests {

	@Autowired
	private SmsCodeRepository smsCodeRepository;

	@Autowired
	private SmsAuthConfig smsAuthConfig;

	@Autowired
	private SmsAuthService smsAuthService;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);

	}

	@Test(expected = InvalidPhoneNumberException.class)
	public void testSendSmsNullNumber() throws SmsAuthException {
		smsAuthService.sendSms(null);
	}

	@Test(expected = InvalidPhoneNumberException.class)
	public void testSendSmsEmptyNumber() throws SmsAuthException {
		smsAuthService.sendSms("");
	}

	@Test
	public void testSendSmsCountSms() throws SmsAuthException {
		String phonenumber = "0123456789";

		Collection<SmsCode> codes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(0L, codes.size());
		smsAuthService.sendSms(phonenumber);
		codes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(1L, codes.size());
	}

	@Test
	public void testCanSendMaxNumberOfSms() throws SmsAuthException {
		String phonenumber = "1";
		for (int i = 0; i < smsAuthConfig.getMaxSmsPerPeriod(); i++) {
			smsAuthService.sendSms(phonenumber);
		}
	}

	@Test(expected = TooManySmsSentException.class)
	public void testSpammingSmsRaiseException() throws SmsAuthException {
		String phonenumber = "2";

		for (int i = 0; i < smsAuthConfig.getMaxSmsPerPeriod(); i++) {
			smsAuthService.sendSms(phonenumber);
		}
		smsAuthService.sendSms(phonenumber);
	}

	@Test
	public void testSendSmsCountSmsFromPeriodOnly() throws SmsAuthException {
		String phonenumber = "3";

		for (int i = 0; i < smsAuthConfig.getMaxSmsPerPeriod(); i++) {
			smsAuthService.sendSms(phonenumber);
		}

		// Set the date of one of the code to be outside of the period
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();
		smsCode.setCreatedAt(Instant.now().minus(Duration.ofMinutes(smsAuthConfig.getPeriodInMinutes() + 1)));
		smsCodeRepository.save(smsCode);

		smsAuthService.sendSms(phonenumber);
	}

	@Test
	public void testVerifySMS() throws SmsAuthException {
		String phonenumber = "4";

		smsAuthService.sendSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();

		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
	}

	@Test(expected = InvalidCodeException.class)
	public void testVerifySMSOnlyValidOnce() throws SmsAuthException {
		String phonenumber = "5";

		smsAuthService.sendSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();

		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
	}

	/* test that expired codes are invalid, even if the code is right */
	@Test(expected = ExpiredCodeException.class)
	public void testExpiredCodeRaiseException() throws SmsAuthException {
		String phonenumber = "6";

		smsAuthService.sendSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();
		smsCode.setCreatedAt(Instant.now().minus(Duration.ofMinutes(smsAuthConfig.getCodeValidityInMinutes() + 1)));
		smsCodeRepository.save(smsCode);

		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
	}

	/* test that a code is disabled if a new one is created */
	@Test
	public void testNewCodeDisablePrevious() throws SmsAuthException {
		String phonenumber = "7";

		smsAuthService.sendSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();
		smsAuthService.sendSms(phonenumber);
		smsCode = smsCodeRepository.findByPhonenumberAndCode(phonenumber, smsCode.getCode()).get();

		assertTrue(smsCode.getDisabled());
	}

	/*
	 * test that a code is always invalid if max number of trials has been exceeded
	 * even if the code is right
	 */
	@Test(expected = TooManyTrialsException.class)
	public void testInvalidCodeAfterMaxTrial() throws SmsAuthException {
		String phonenumber = "8";

		smsAuthService.sendSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();

		for (int i = 0; i < smsAuthConfig.getMaxTrialsPerCode(); i++) {
			try {
				smsAuthService.validateSmsCode(phonenumber, "wrongcode");
			} catch (WrongCodeException e) {

			}
		}
		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
	}

	/*
	 * Test that correct code do not enable the previous one to be used even if it
	 * has the right code
	 */
	@Test
	public void testSuccessfulVerificationDoNotActivatePreviousCode() throws SmsAuthException {
		String phonenumber = "9";

		smsAuthService.sendSms(phonenumber);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}
		smsAuthService.sendSms(phonenumber);
		SmsCode smsCode = smsCodeRepository
				.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(phonenumber, Instant.MIN).get(0);
		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());

		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(1, smsCodes.size());
		smsCode = smsCodes.iterator().next();
		assertTrue(smsCode.getDisabled());

		try {
			smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
		} catch (ExpiredCodeException e) {
			return;
		}
		assertTrue(false);
	}
}
