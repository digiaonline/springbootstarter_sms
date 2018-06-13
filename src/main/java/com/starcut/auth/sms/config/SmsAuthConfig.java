package com.starcut.auth.sms.config;

public class SmsAuthConfig {

	private String shortCode;

	private int codeLength;

	private int maxRetryPerDay;

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public int getMaxRetryPerDay() {
		return maxRetryPerDay;
	}

	public void setMaxRetryPerDay(int maxRetryPerDay) {
		this.maxRetryPerDay = maxRetryPerDay;
	}

	public int getCodeLength() {
		return codeLength;
	}

	public void setCodeLength(int codeLength) {
		this.codeLength = codeLength;
	}

}
