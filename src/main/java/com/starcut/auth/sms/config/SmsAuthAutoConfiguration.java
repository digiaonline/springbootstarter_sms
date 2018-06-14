package com.starcut.auth.sms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.starcut.auth.sms.service.SmsAuthService;

@Configuration
@ConditionalOnClass(SmsAuthService.class)
@EnableConfigurationProperties(SmsAuthProperties.class)
@ComponentScan
@EntityScan("com.starcut.auth.sms.db")
@EnableJpaRepositories("com.starcut.auth.sms.db")
public class SmsAuthAutoConfiguration {

	private final int DEFAULT_MAX_SMS_PER_PERIOD = 5;

	private final int DEFAULT_PERIOD_IN_MINUTES = 24 * 60; // 1 Day

	private final int DEFAULT_CODE_VALIDITY_IN_MINUTES = 10;

	private final int DEFAULT_CODE_LENGTH = 6;

	private final int DEFAULT_MAX_TRIALS_PER_CODE = 3;

	@Autowired
	private SmsAuthProperties smsAuthProperties;

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

		SmsAuthConfig authSmsConfig = new SmsAuthConfig();
		authSmsConfig.setShortCode(shortCode);
		authSmsConfig.setMaxSmsPerPeriod(maxSmsPerPeriod);
		authSmsConfig.setPeriodInMinutes(periodInMinutes);
		authSmsConfig.setCodeLength(codeLength);
		authSmsConfig.setCodeValidityInMinutes(codeValidityInMinutes);
		authSmsConfig.setMaxTrialsPerCode(maxTrialsPerCode);

		return authSmsConfig;
	}

	@Bean
	@ConditionalOnMissingBean
	public SmsAuthService smsAuthService(SmsAuthConfig authSmsConfig) {
		return new SmsAuthService(authSmsConfig);
	}
}
