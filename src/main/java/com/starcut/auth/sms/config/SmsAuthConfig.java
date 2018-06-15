package com.starcut.auth.sms.config;

import java.util.List;

public class SmsAuthConfig {

	private String shortCode;

	private int codeLength;

	private int maxSmsPerPeriod;

	private int periodInMinutes;

	private int codeValidityInMinutes;

	private int maxTrialsPerCode;

	private String region;

	private String senderId;

	private List<Integer> allowedRegion;

	private int minTimeBetweenTwoSmsInSecond;

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

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public List<Integer> getAllowedRegion() {
		return allowedRegion;
	}

	public void setAllowedRegion(List<Integer> allowedRegion) {
		this.allowedRegion = allowedRegion;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public int getMinTimeBetweenTwoSmsInSecond() {
		return minTimeBetweenTwoSmsInSecond;
	}

	public void setMinTimeBetweenTwoSmsInSecond(int minTimeBetweenTwoSmsInSecond) {
		this.minTimeBetweenTwoSmsInSecond = minTimeBetweenTwoSmsInSecond;
	}

}
