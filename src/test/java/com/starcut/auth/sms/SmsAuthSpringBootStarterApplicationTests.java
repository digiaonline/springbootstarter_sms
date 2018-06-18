package com.starcut.auth.sms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import com.starcut.auth.sms.service.SmsSenderService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestConfiguration(value = "com.starcut.auth.sms.config.SmsAuthAutoConfiguration")
public class SmsAuthSpringBootStarterApplicationTests {

	@Autowired
	private SmsCodeRepository smsCodeRepository;

	@Autowired
	private SmsAuthConfig smsAuthConfig;

	@MockBean
	private SmsSenderService smsSenderService;

	@InjectMocks
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

	@Test(expected = InvalidPhoneNumberException.class)
	public void testSendSmsInvalidNumber() throws SmsAuthException {
		smsAuthService.sendSms("0123");
	}

	@Test(expected = InvalidPhoneNumberException.class)
	public void testSendSmsWrongRegionNumber() throws SmsAuthException {
		smsAuthService.sendSms("+39 3 12345678");
	}

	@Test
	public void testSendSmsAllowedRegionNumber() throws SmsAuthException {
		smsAuthService.sendSms("+33 3 12345678");
	}

	@Test
	public void testSendSmsCountSms() throws SmsAuthException {
		String phonenumber = "+35840123450";

		Collection<SmsCode> codes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(0L, codes.size());
		smsAuthService.sendSms(phonenumber);
		codes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(1L, codes.size());
	}

	@Test
	public void testCanSendMaxNumberOfSms() throws SmsAuthException {
		String phonenumber = "+35840123451";
		for (int i = 0; i < smsAuthConfig.getMaxSmsPerPeriod(); i++) {
			smsAuthService.sendSms(phonenumber);
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test(expected = TooManySmsSentException.class)
	public void testSpammingSmsRaiseException() throws SmsAuthException {
		String phonenumber = "+35840123452";

		for (int i = 0; i < smsAuthConfig.getMaxSmsPerPeriod(); i++) {
			smsAuthService.sendSms(phonenumber);
		}
		smsAuthService.sendSms(phonenumber);
	}

	@Test
	public void testSendSmsCountSmsFromPeriodOnly() throws SmsAuthException {
		String phonenumber = "+35840123453";

		for (int i = 0; i < smsAuthConfig.getMaxSmsPerPeriod(); i++) {
			smsAuthService.sendSms(phonenumber);
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		String phonenumber = "+35840123454";

		smsAuthService.sendSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();

		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
	}

	@Test(expected = InvalidCodeException.class)
	public void testVerifySMSOnlyValidOnce() throws SmsAuthException {
		String phonenumber = "+35840123455";

		smsAuthService.sendSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();

		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
	}

	/* test that expired codes are invalid, even if the code is right */
	@Test(expected = ExpiredCodeException.class)
	public void testExpiredCodeRaiseException() throws SmsAuthException {
		String phonenumber = "+35840123456";

		smsAuthService.sendSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();
		smsCode.setCreatedAt(Instant.now().minus(Duration.ofMinutes(smsAuthConfig.getCodeValidityInMinutes() + 1)));
		smsCodeRepository.save(smsCode);

		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
	}

	/* test that a code is disabled if a new one is created */
	@Test
	public void testSuccessfulVerificationMarksSmsAsValidated() throws SmsAuthException {
		String phonenumber = "+35840123457";

		smsAuthService.sendSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
		smsCode = smsCodeRepository.findByPhonenumberAndCode(phonenumber, smsCode.getCode()).get();

		assertTrue(smsCode.getValidated());
	}

	/*
	 * test that a code is always invalid if max number of trials has been exceeded
	 * even if the code is right
	 */
	@Test(expected = TooManyTrialsException.class)
	public void testInvalidCodeAfterMaxTrial() throws SmsAuthException {
		String phonenumber = "040123458";
		String formattedPhoneNumber = "+35840123458";

		smsAuthService.sendSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(formattedPhoneNumber);
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
	 * Test that two messages cannot be sent within the same second
	 */
	@Test(expected = TooManySmsSentException.class)
	public void testCannotFloodANumber() throws SmsAuthException {
		String phonenumber = "+35840123460";
		smsAuthService.sendSms(phonenumber);
		smsAuthService.sendSms(phonenumber);
	}

	/*
	 * Test that correct code do not enable the previous one to be used even if it
	 * has the right code
	 */
	@Test
	public void testSuccessfulVerificationDoNotActivatePreviousCode() throws SmsAuthException {
		String phonenumber = "+35840123459";

		smsAuthService.sendSms(phonenumber);

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e1) {
		}
		smsAuthService.sendSms(phonenumber);
		SmsCode smsCode = smsCodeRepository
				.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(phonenumber, Instant.MIN).get(0);
		smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());

		Collection<SmsCode> smsCodes = smsCodeRepository
				.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(phonenumber,
						Instant.MIN);
		assertEquals(2, smsCodes.size());
		smsCode = smsCodes.iterator().next();
		assertTrue(smsCode.getValidated());

		try {
			smsAuthService.validateSmsCode(phonenumber, smsCode.getCode());
		} catch (ExpiredCodeException e) {
			return;
		}
		assertTrue(false);
	}
}
