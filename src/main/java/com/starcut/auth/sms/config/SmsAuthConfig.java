package com.starcut.auth.sms.config;

public class SmsAuthConfig {

	private String shortCode;

	private int codeLength;

	private int maxSmsPerPeriod;

	private int periodInMinutes;

	private int codeValidityInMinutes;

	private int maxTrialsPerCode;

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public int getCodeLength() {
		return codeLength;
	}

	public void setCodeLength(int codeLength) {
		this.codeLength = codeLength;
	}

	public int getMaxSmsPerPeriod() {
		return maxSmsPerPeriod;
	}

	public void setMaxSmsPerPeriod(int maxSmsPerPeriod) {
		this.maxSmsPerPeriod = maxSmsPerPeriod;
	}

	public int getPeriodInMinutes() {
		return periodInMinutes;
	}

	public void setPeriodInMinutes(int periodInMinutes) {
		this.periodInMinutes = periodInMinutes;
	}

	public int getCodeValidityInMinutes() {
		return codeValidityInMinutes;
	}

	public void setCodeValidityInMinutes(int codeValidityInMinutes) {
		this.codeValidityInMinutes = codeValidityInMinutes;
	}

	public int getMaxTrialsPerCode() {
		return maxTrialsPerCode;
	}

	public void setMaxTrialsPerCode(int maxTrialsPerCode) {
		this.maxTrialsPerCode = maxTrialsPerCode;
	}

}
