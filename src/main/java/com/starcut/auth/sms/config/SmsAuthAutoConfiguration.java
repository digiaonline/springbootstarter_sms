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

	private final int DEFAULT_MAX_RETRY_PER_DAY = 5;

	private final int DEFAULT_CODE_LENGTH = 6;
	
	@Autowired
	private SmsAuthProperties smsAuthProperties;
	
	@Bean
	@ConditionalOnMissingBean
	public SmsAuthConfig authSmsConfig() {

		String shortCode = smsAuthProperties.getShortCode() == null ? "" : smsAuthProperties.getShortCode();
		Integer maxRetryPerDay = smsAuthProperties.getMaxRetryPerDay() == null ? DEFAULT_MAX_RETRY_PER_DAY
				: smsAuthProperties.getMaxRetryPerDay();
		Integer codeLength = smsAuthProperties.getCodeLength() == null ? DEFAULT_CODE_LENGTH : smsAuthProperties.getCodeLength();

		SmsAuthConfig authSmsConfig = new SmsAuthConfig();
		authSmsConfig.setShortCode(shortCode);
		authSmsConfig.setMaxRetryPerDay(maxRetryPerDay);
		authSmsConfig.setCodeLength(codeLength);

		return authSmsConfig;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public SmsAuthService smsAuthService(SmsAuthConfig authSmsConfig) {
		return new SmsAuthService(authSmsConfig);
	}
}
