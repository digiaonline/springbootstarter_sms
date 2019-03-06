//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.config;

import java.util.List;

public class SmsAuthConfig {
    private String shortCode;
    private int codeLength;
    private int maxSmsPerPeriod;
    private int periodInMinutes;
    private int codeValidityInMinutes;
    private int maxTrialsPerCode;
    private int transferDelayInHours;
    private String easterEggPhoneNumber;
    private String easterEggCode;
    private String region;
    private String senderId;
    private List<Integer> allowedRegion;
    private int minTimeBetweenTwoSmsInSecond;

    public SmsAuthConfig() {
    }

    public String getShortCode() {
        return this.shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public int getCodeLength() {
        return this.codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public int getMaxSmsPerPeriod() {
        return this.maxSmsPerPeriod;
    }

    public void setMaxSmsPerPeriod(int maxSmsPerPeriod) {
        this.maxSmsPerPeriod = maxSmsPerPeriod;
    }

    public int getPeriodInMinutes() {
        return this.periodInMinutes;
    }

    public void setPeriodInMinutes(int periodInMinutes) {
        this.periodInMinutes = periodInMinutes;
    }

    public int getCodeValidityInMinutes() {
        return this.codeValidityInMinutes;
    }

    public void setCodeValidityInMinutes(int codeValidityInMinutes) {
        this.codeValidityInMinutes = codeValidityInMinutes;
    }

    public int getMaxTrialsPerCode() {
        return this.maxTrialsPerCode;
    }

    public void setMaxTrialsPerCode(int maxTrialsPerCode) {
        this.maxTrialsPerCode = maxTrialsPerCode;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<Integer> getAllowedRegion() {
        return this.allowedRegion;
    }

    public void setAllowedRegion(List<Integer> allowedRegion) {
        this.allowedRegion = allowedRegion;
    }

    public String getSenderId() {
        return this.senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public int getMinTimeBetweenTwoSmsInSecond() {
        return this.minTimeBetweenTwoSmsInSecond;
    }

    public void setMinTimeBetweenTwoSmsInSecond(int minTimeBetweenTwoSmsInSecond) {
        this.minTimeBetweenTwoSmsInSecond = minTimeBetweenTwoSmsInSecond;
    }

    public int getTransferDelayInHours() {
        return this.transferDelayInHours;
    }

    public void setTransferDelayInHours(int transferDelayInHours) {
        this.transferDelayInHours = transferDelayInHours;
    }

    public String getEasterEggPhoneNumber() {
        return this.easterEggPhoneNumber;
    }

    public void setEasterEggPhoneNumber(String easterEggPhoneNumber) {
        this.easterEggPhoneNumber = easterEggPhoneNumber;
    }

    public String getEasterEggCode() {
        return this.easterEggCode;
    }

    public void setEasterEggCode(String easterEggCode) {
        this.easterEggCode = easterEggCode;
    }
}
