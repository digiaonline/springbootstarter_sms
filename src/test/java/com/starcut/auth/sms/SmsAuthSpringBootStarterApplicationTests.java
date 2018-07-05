package com.starcut.auth.sms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import com.starcut.auth.sms.config.SmsAuthConfig;
import com.starcut.auth.sms.db.PhoneUuidRepository;
import com.starcut.auth.sms.db.SmsCodeRepository;
import com.starcut.auth.sms.db.entity.PhoneUuid;
import com.starcut.auth.sms.db.entity.SmsCode;
import com.starcut.auth.sms.db.entity.SmsCodeId;
import com.starcut.auth.sms.db.entity.type.SmsCodeType;
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
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class SmsAuthSpringBootStarterApplicationTests {

	@Autowired
	private SmsCodeRepository smsCodeRepository;

	@Autowired
	private PhoneUuidRepository phoneUuidRepository;

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
		smsAuthService.sendValidationSms(null);
	}

	@Test(expected = InvalidPhoneNumberException.class)
	public void testSendSmsEmptyNumber() throws SmsAuthException {
		smsAuthService.sendValidationSms("");
	}

	@Test(expected = InvalidPhoneNumberException.class)
	public void testSendSmsInvalidNumber() throws SmsAuthException {
		smsAuthService.sendValidationSms("0123");
	}

	@Test(expected = InvalidPhoneNumberException.class)
	public void testSendSmsWrongRegionNumber() throws SmsAuthException {
		smsAuthService.sendValidationSms("+39 3 12345678");
	}

	@Test
	public void testSendSmsAllowedRegionNumber() throws SmsAuthException {
		smsAuthService.sendValidationSms("+33 3 12345678");
	}

	@Test
	public void testSendSmsCountSms() throws SmsAuthException {
		String phonenumber = "+35840123450";

		Collection<SmsCode> codes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(0L, codes.size());
		smsAuthService.sendValidationSms(phonenumber);
		codes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(1L, codes.size());
	}

	@Test
	public void testCanSendMaxNumberOfSms() throws SmsAuthException {
		String phonenumber = "+35840123451";
		for (int i = 0; i < smsAuthConfig.getMaxSmsPerPeriod(); i++) {
			smsAuthService.sendValidationSms(phonenumber);
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
			smsAuthService.sendValidationSms(phonenumber);
		}
		smsAuthService.sendValidationSms(phonenumber);
	}

	@Test
	public void testSendSmsCountSmsFromPeriodOnly() throws SmsAuthException {
		String phonenumber = "+35840123453";

		for (int i = 0; i < smsAuthConfig.getMaxSmsPerPeriod(); i++) {
			smsAuthService.sendValidationSms(phonenumber);
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

		smsAuthService.sendValidationSms(phonenumber);
	}

	@Test
	public void testVerifySMS() throws SmsAuthException {
		String phonenumber = "+35840123454";

		smsAuthService.sendValidationSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();

		smsAuthService.verifyValidationSmsCode(phonenumber, smsCode.getCode());
	}

	@Test(expected = InvalidCodeException.class)
	public void testVerifySMSOnlyValidOnce() throws SmsAuthException {
		String phonenumber = "+35840123455";

		smsAuthService.sendValidationSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();

		smsAuthService.verifyValidationSmsCode(phonenumber, smsCode.getCode());
		smsAuthService.verifyValidationSmsCode(phonenumber, smsCode.getCode());
	}

	/* test that expired codes are invalid, even if the code is right */
	@Test(expected = ExpiredCodeException.class)
	public void testExpiredCodeRaiseException() throws SmsAuthException {
		String phonenumber = "+35840123456";

		smsAuthService.sendValidationSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();
		smsCode.setCreatedAt(Instant.now().minus(Duration.ofMinutes(smsAuthConfig.getCodeValidityInMinutes() + 1)));
		smsCodeRepository.save(smsCode);

		smsAuthService.verifyValidationSmsCode(phonenumber, smsCode.getCode());
	}

	/* test that a code is disabled if a new one is created */
	@Test
	public void testSuccessfulVerificationMarksSmsAsValidated() throws SmsAuthException {
		String phonenumber = "+35840123457";

		smsAuthService.sendValidationSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(phonenumber);
		SmsCode smsCode = smsCodes.iterator().next();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		smsAuthService.verifyValidationSmsCode(phonenumber, smsCode.getCode());
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

		smsAuthService.sendValidationSms(phonenumber);
		Collection<SmsCode> smsCodes = smsCodeRepository.findAllByPhonenumber(formattedPhoneNumber);
		SmsCode smsCode = smsCodes.iterator().next();

		for (int i = 0; i < smsAuthConfig.getMaxTrialsPerCode(); i++) {
			try {
				smsAuthService.verifyValidationSmsCode(phonenumber, "wrongcode");
			} catch (WrongCodeException e) {

			}
		}
		smsAuthService.verifyValidationSmsCode(phonenumber, smsCode.getCode());
	}

	/*
	 * Test that two messages cannot be sent within the same second
	 */
	@Test(expected = TooManySmsSentException.class)
	public void testCannotFloodANumber() throws SmsAuthException {
		String phonenumber = "+35840123460";
		smsAuthService.sendValidationSms(phonenumber);
		smsAuthService.sendValidationSms(phonenumber);
	}

	}

	/*
	 * Test that correct code do not enable the previous one to be used even if it
	 * has the right code
	 */
	@Test
	public void testSuccessfulVerificationDoNotActivatePreviousCode() throws SmsAuthException {
		String phonenumber = "+35840123459";

		smsAuthService.sendValidationSms(phonenumber);

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e1) {
		}
		smsAuthService.sendValidationSms(phonenumber);
		SmsCode smsCode = smsCodeRepository
				.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(phonenumber, Instant.MIN).get(0);
		smsAuthService.verifyValidationSmsCode(phonenumber, smsCode.getCode());

		Collection<SmsCode> smsCodes = smsCodeRepository
				.findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(phonenumber, Instant.MIN);
		assertEquals(2, smsCodes.size());
		smsCode = smsCodes.iterator().next();
		assertTrue(smsCode.getValidated());

		try {
			smsAuthService.verifyValidationSmsCode(phonenumber, smsCode.getCode());
		} catch (ExpiredCodeException e) {
			return;
		}
		assertTrue(false);
	}

	@Test
	public void testSendSecureSMSDoesNotRegisterUuid() throws InvalidPhoneNumberException, TooManySmsSentException {
		String phonenumber = "+35840123459";
		smsAuthService.sendSmsSecure(phonenumber, "fakeuuid");
		PhoneUuid phoneUuid = phoneUuidRepository.findById(phonenumber).orElse(null);
		assertNull(phoneUuid);
	}

	@Test
	public void testVerifySecureRegisterUuid()
			throws InvalidPhoneNumberException, TooManySmsSentException, InvalidCodeException {
		String phonenumber = "+35840123459";
		smsAuthService.sendSmsSecure(phonenumber, "fakeuuid");
		List<SmsCode> smsCodes = (List<SmsCode>) smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(1, smsCodes.size());
		SmsCode smsCode = smsCodes.get(0);
		assertEquals(SmsCodeType.VALIDATION, smsCode.getType());
		String code = smsCode.getCode();
		smsAuthService.validateSmsCodeSecure(phonenumber, "UUID", code);
		PhoneUuid phoneUuid = phoneUuidRepository.findById(phonenumber).orElse(null);
		assertNotNull(phoneUuid);
		assertEquals("UUID", phoneUuid.getUuid());
		assertNull(phoneUuid.getNewUuid());
		assertNull(phoneUuid.getChangeRequestedAt());

	}

	@Test
	public void testInvalidUuidPreventSmsSending() throws InvalidPhoneNumberException, TooManySmsSentException {
		String phonenumber = "+35840123459";
		PhoneUuid phoneUuid = new PhoneUuid();
		phoneUuid.setPhoneNumber(phonenumber);
		phoneUuid.setUuid("UUID");
		phoneUuidRepository.save(phoneUuid);
		smsAuthService.sendSmsSecure(phonenumber, "wrongUUID");
		List<SmsCode> smsCodes = (List<SmsCode>) smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(0, smsCodes.size());
	}

	@Test
	public void testInvalidUuidPreventSmsVerification() throws InvalidPhoneNumberException {
		String phonenumber = "+35840123459";
		PhoneUuid phoneUuid = new PhoneUuid();
		phoneUuid.setPhoneNumber(phonenumber);
		phoneUuid.setUuid("UUID");
		phoneUuidRepository.save(phoneUuid);
		SmsCode smsCode = new SmsCode();
		SmsCodeId smsCodeId = new SmsCodeId();
		smsCodeId.setCode("correct code");
		smsCodeId.setPhonenumber(phonenumber);
		smsCode.setId(smsCodeId);
		smsCode.setType(SmsCodeType.VALIDATION);
		smsCodeRepository.save(smsCode);
		try {
			smsAuthService.validateSmsCodeSecure(phonenumber, "wrong UUID", "wrong code");
			assertTrue(false);
		} catch (InvalidCodeException e) {
		}
		try {
			smsAuthService.validateSmsCodeSecure(phonenumber, "wrong UUID", "correct code");
			assertTrue(false);
		} catch (InvalidCodeException e) {
		}
	}

	@Test
	public void testValidUuidAllowSmsSending() throws InvalidPhoneNumberException, TooManySmsSentException {
		String phonenumber = "+35840123459";
		PhoneUuid phoneUuid = new PhoneUuid();
		phoneUuid.setPhoneNumber(phonenumber);
		phoneUuid.setUuid("correct UUID");
		phoneUuidRepository.save(phoneUuid);
		smsAuthService.sendSmsSecure(phonenumber, "correct UUID");
		List<SmsCode> smsCodes = (List<SmsCode>) smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(1, smsCodes.size());
	}

	@Test
	public void testValidUuidAllowSmsVerification() throws InvalidPhoneNumberException {
		String phonenumber = "+35840123459";
		PhoneUuid phoneUuid = new PhoneUuid();
		phoneUuid.setPhoneNumber(phonenumber);
		phoneUuid.setUuid("UUID");
		phoneUuidRepository.save(phoneUuid);
		SmsCode smsCode = new SmsCode();
		SmsCodeId smsCodeId = new SmsCodeId();
		smsCodeId.setCode("correct code");
		smsCodeId.setPhonenumber(phonenumber);
		smsCode.setId(smsCodeId);
		smsCode.setType(SmsCodeType.VALIDATION);
		smsCodeRepository.save(smsCode);

		try {
			smsAuthService.validateSmsCodeSecure(phonenumber, "UUID", "wrong code");
			assertTrue(false);
		} catch (InvalidCodeException e) {
		}
		try {
			smsAuthService.validateSmsCodeSecure(phonenumber, "UUID", "correct code");
		} catch (InvalidCodeException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testSendResetSms() throws InvalidPhoneNumberException, TooManySmsSentException {
		String phonenumber = "+35840123459";
		PhoneUuid phoneUuid = new PhoneUuid();
		phoneUuid.setPhoneNumber(phonenumber);
		phoneUuid.setUuid("UUID");
		phoneUuidRepository.save(phoneUuid);
		smsAuthService.sendResetSms(phonenumber);
		List<SmsCode> smsCodes = (List<SmsCode>) smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(1, smsCodes.size());
		SmsCode smsCode = smsCodes.get(0);
		assertEquals(SmsCodeType.RESET, smsCode.getType());
		phoneUuid = phoneUuidRepository.findById(phonenumber).orElse(null);
		assertEquals("UUID", phoneUuid.getUuid());
		assertNull(phoneUuid.getNewUuid());
		assertNull(phoneUuid.getChangeRequestedAt());
	}

	@Test
	public void testValidatingResetUuidDoesNotRequireValidUuid()
			throws InvalidCodeException, InvalidPhoneNumberException {
		String phonenumber = "+35840123459";
		PhoneUuid phoneUuid = new PhoneUuid();
		phoneUuid.setPhoneNumber(phonenumber);
		phoneUuid.setUuid("UUID");
		phoneUuidRepository.save(phoneUuid);
		SmsCode smsCode = new SmsCode();
		SmsCodeId smsCodeId = new SmsCodeId();
		smsCodeId.setCode("correct code");
		;
		smsCodeId.setPhonenumber(phonenumber);
		smsCode.setId(smsCodeId);
		smsCode.setType(SmsCodeType.RESET);
		smsCodeRepository.save(smsCode);
		smsAuthService.verifyResetSms(phonenumber, "new UUID", "correct code", "Warning message");
		List<SmsCode> smsCodes = (List<SmsCode>) smsCodeRepository.findAllByPhonenumber(phonenumber);
		assertEquals(1, smsCodes.size());
		smsCode = smsCodes.get(0);
		assertEquals(SmsCodeType.RESET, smsCode.getType());
		phoneUuid = phoneUuidRepository.findById(phonenumber).orElse(null);
		assertEquals("UUID", phoneUuid.getUuid());
		assertEquals("new UUID", phoneUuid.getNewUuid());
		assertNotNull(phoneUuid.getChangeRequestedAt());
	}

	@Test(expected = InvalidCodeException.class)
	public void testResetSMSCannotBeUsedForLogin() throws InvalidCodeException, InvalidPhoneNumberException {
		String phonenumber = "+35840123459";
		PhoneUuid phoneUuid = new PhoneUuid();
		phoneUuid.setPhoneNumber(phonenumber);
		phoneUuid.setUuid("UUID");
		phoneUuidRepository.save(phoneUuid);
		SmsCode smsCode = new SmsCode();
		SmsCodeId smsCodeId = new SmsCodeId();
		smsCodeId.setCode("correct code");
		;
		smsCodeId.setPhonenumber(phonenumber);
		smsCode.setId(smsCodeId);
		smsCode.setType(SmsCodeType.RESET);
		smsCodeRepository.save(smsCode);
		smsAuthService.validateSmsCodeSecure(phonenumber, "UUID", "correct code");
	}
}
