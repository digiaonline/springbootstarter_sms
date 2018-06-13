package com.starcut.auth.sms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "starcut.auth.sms")
public class SmsAuthProperties {

	private String shortCode; // dedicated short code for the application if any
	private Integer maxRetryPerDay;
	private Integer codeLength;

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public Integer getMaxRetryPerDay() {
		return maxRetryPerDay;
	}

	public void setMaxRetryPerDay(Integer maxRetryPerDay) {
		this.maxRetryPerDay = maxRetryPerDay;
	}

	public Integer getCodeLength() {
		return codeLength;
	}

	public void setCodeLength(Integer codeLength) {
		this.codeLength = codeLength;
	}

}
