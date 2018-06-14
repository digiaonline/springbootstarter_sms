package com.starcut.auth.sms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "starcut.auth.sms")
public class SmsAuthProperties {

	private String shortCode; // dedicated short code for the application if any
	private Integer codeLength;

	private Integer maxSmsPerPeriod;

	private Integer periodInMinutes;

	private Integer codeValidityInMinutes;

	private Integer maxTrialsPerCode;

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public Integer getCodeLength() {
		return codeLength;
	}

	public void setCodeLength(Integer codeLength) {
		this.codeLength = codeLength;
	}

	public Integer getMaxSmsPerPeriod() {
		return maxSmsPerPeriod;
	}

	public void setMaxSmsPerPeriod(Integer maxSmsPerPeriod) {
		this.maxSmsPerPeriod = maxSmsPerPeriod;
	}

	public Integer getPeriodInMinutes() {
		return periodInMinutes;
	}

	public void setPeriodInMinutes(Integer periodInMinutes) {
		this.periodInMinutes = periodInMinutes;
	}

	public Integer getCodeValidityInMinutes() {
		return codeValidityInMinutes;
	}

	public void setCodeValidityInMinutes(Integer codeValidityInMinutes) {
		this.codeValidityInMinutes = codeValidityInMinutes;
	}

	public Integer getMaxTrialsPerCode() {
		return maxTrialsPerCode;
	}

	public void setMaxTrialsPerCode(Integer maxTrialsPerCode) {
		this.maxTrialsPerCode = maxTrialsPerCode;
	}

}