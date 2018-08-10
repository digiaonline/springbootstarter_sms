package com.starcut.auth.sms.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.starcut.auth.sms.service.SmsAuthService;
import com.starcut.auth.sms.service.SmsSenderService;

@Configuration
@EnableConfigurationProperties(SmsAuthProperties.class)
@ComponentScan
@EntityScan("com.starcut.auth.sms.db")
@EnableJpaRepositories("com.starcut.auth.sms.db")
public class SmsAuthAutoConfiguration {

	private static final int DEFAULT_MIN_TIME_BETWEEN_TWO_SMS_IN_SECONDS = 1;

	private static final int DEFAULT_MAX_SMS_PER_PERIOD = 5;

	private static final int DEFAULT_PERIOD_IN_MINUTES = 24 * 60; // 1 Day

	private static final int DEFAULT_CODE_VALIDITY_IN_MINUTES = 10;

	private static final int DEFAULT_CODE_LENGTH = 6;

	private static final int DEFAULT_MAX_TRIALS_PER_CODE = 3;

	private static final int DEFAULT_TRANSFER_DELAY_IN_HOURS = 24;

	private static final String DEFAULT_REGION = "FI";

	private static final List<String> DEFAULT_ALLOWED_REGIONS = new ArrayList<>();

	private static final String DEFAULT_SENDER_ID = "Starcut";

	private static final String DEFAULT_EASTER_EGG_PHONE_NUMBER = "+358999999";

	private static final String DEFAULT_EASTER_EGG_CODE = "123456";

	@Autowired
	private SmsAuthProperties smsAuthProperties;

	private PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

	@Bean
	@ConditionalOnMissingBean
	public SmsAuthConfig authSmsConfig() {

		String shortCode = smsAuthProperties.getShortCode() == null ? "" : smsAuthProperties.getShortCode();
		Integer maxSmsPerPeriod = smsAuthProperties.getMaxSmsPerPeriod() == null ? DEFAULT_MAX_SMS_PER_PERIOD
				: smsAuthProperties.getMaxSmsPerPeriod();
		Integer periodInMinutes = smsAuthProperties.getPeriodInMinutes() == null ? DEFAULT_PERIOD_IN_MINUTES
				: smsAuthProperties.getPeriodInMinutes();
		Integer codeLength = smsAuthProperties.getCodeLength() == null ? DEFAULT_CODE_LENGTH
				: smsAuthProperties.getCodeLength();
		Integer codeValidityInMinutes = smsAuthProperties.getCodeValidityInMinutes() == null
				? DEFAULT_CODE_VALIDITY_IN_MINUTES
				: smsAuthProperties.getCodeValidityInMinutes();
		Integer maxTrialsPerCode = smsAuthProperties.getMaxTrialsPerCode() == null ? DEFAULT_MAX_TRIALS_PER_CODE
				: smsAuthProperties.getMaxTrialsPerCode();
		Integer transferDelayInHours = smsAuthProperties.getTransferDelayInHours() == null
				? DEFAULT_TRANSFER_DELAY_IN_HOURS
				: smsAuthProperties.getTransferDelayInHours();
		String region = smsAuthProperties.getRegion() == null ? DEFAULT_REGION : smsAuthProperties.getRegion();
		List<String> allowedRegions = smsAuthProperties.getAllowedRegions() == null ? DEFAULT_ALLOWED_REGIONS
				: Arrays.asList(smsAuthProperties.getAllowedRegions().split(","));
		String senderId = smsAuthProperties.getSenderId() == null ? DEFAULT_SENDER_ID : smsAuthProperties.getSenderId();
		Integer minTimeBetweenTwoSmsInSecond = smsAuthProperties.getMinTimeBetweenTwoSmsInSeconds() == null
				? DEFAULT_MIN_TIME_BETWEEN_TWO_SMS_IN_SECONDS
				: smsAuthProperties.getMinTimeBetweenTwoSmsInSeconds();
		String easterEggPhoneNumber = smsAuthProperties.getEasterEggPhoneNumber() == null
				? DEFAULT_EASTER_EGG_PHONE_NUMBER
				: smsAuthProperties.getEasterEggPhoneNumber();
		String easterEggCode = smsAuthProperties.getEasterEggCode() == null
				? DEFAULT_EASTER_EGG_CODE
				: smsAuthProperties.getEasterEggCode();

		SmsAuthConfig authSmsConfig = new SmsAuthConfig();
		authSmsConfig.setShortCode(shortCode);
		authSmsConfig.setMaxSmsPerPeriod(maxSmsPerPeriod);
		authSmsConfig.setPeriodInMinutes(periodInMinutes);
		authSmsConfig.setCodeLength(codeLength);
		authSmsConfig.setCodeValidityInMinutes(codeValidityInMinutes);
		authSmsConfig.setMaxTrialsPerCode(maxTrialsPerCode);
		authSmsConfig.setRegion(region);
		authSmsConfig.setAllowedRegion(allowedRegions.stream()
				.map(code -> phoneNumberUtil.getCountryCodeForRegion(code.trim())).collect(Collectors.toList()));
		authSmsConfig.setSenderId(senderId);
		authSmsConfig.setTransferDelayInHours(transferDelayInHours);
		authSmsConfig.setMinTimeBetweenTwoSmsInSecond(minTimeBetweenTwoSmsInSecond);
		authSmsConfig.setEasterEggPhoneNumber(easterEggPhoneNumber);
		authSmsConfig.setEasterEggCode(easterEggCode);

		return authSmsConfig;
	}

	@Bean
	@ConditionalOnMissingBean
	public SmsAuthService smsAuthService(SmsAuthConfig authSmsConfig) {
		return new SmsAuthService(authSmsConfig);
	}

	@Bean
	@ConditionalOnMissingBean
	public SmsSenderService smsSenderService() {
		return new SmsSenderService();
	}
}
