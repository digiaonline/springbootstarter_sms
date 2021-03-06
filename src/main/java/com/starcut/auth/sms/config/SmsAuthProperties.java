//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "starcut.auth.sms"
)
public class SmsAuthProperties {
    private String shortCode;
    private Integer codeLength;
    private Integer maxSmsPerPeriod;
    private Integer periodInMinutes;
    private Integer codeValidityInMinutes;
    private Integer maxTrialsPerCode;
    private String region;
    private String allowedRegions;
    private String senderId;
    private Integer transferDelayInHours;
    private Integer minTimeBetweenTwoSmsInSeconds;
    private String easterEggPhoneNumber;
    private String easterEggCode;

    public SmsAuthProperties() {
    }

    public String getShortCode() {
        return this.shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public Integer getCodeLength() {
        return this.codeLength;
    }

    public void setCodeLength(Integer codeLength) {
        this.codeLength = codeLength;
    }

    public Integer getMaxSmsPerPeriod() {
        return this.maxSmsPerPeriod;
    }

    public void setMaxSmsPerPeriod(Integer maxSmsPerPeriod) {
        this.maxSmsPerPeriod = maxSmsPerPeriod;
    }

    public Integer getPeriodInMinutes() {
        return this.periodInMinutes;
    }

    public void setPeriodInMinutes(Integer periodInMinutes) {
        this.periodInMinutes = periodInMinutes;
    }

    public Integer getCodeValidityInMinutes() {
        return this.codeValidityInMinutes;
    }

    public void setCodeValidityInMinutes(Integer codeValidityInMinutes) {
        this.codeValidityInMinutes = codeValidityInMinutes;
    }

    public Integer getMaxTrialsPerCode() {
        return this.maxTrialsPerCode;
    }

    public void setMaxTrialsPerCode(Integer maxTrialsPerCode) {
        this.maxTrialsPerCode = maxTrialsPerCode;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAllowedRegions() {
        return this.allowedRegions;
    }

    public void setAllowedRegions(String allowedRegions) {
        this.allowedRegions = allowedRegions;
    }

    public String getSenderId() {
        return this.senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Integer getMinTimeBetweenTwoSmsInSeconds() {
        return this.minTimeBetweenTwoSmsInSeconds;
    }

    public void setMinTimeBetweenTwoSmsInSeconds(Integer minTimeBetweenTwoSmsInSeconds) {
        this.minTimeBetweenTwoSmsInSeconds = minTimeBetweenTwoSmsInSeconds;
    }

    public Integer getTransferDelayInHours() {
        return this.transferDelayInHours;
    }

    public void setTransferDelayInHours(Integer transferDelayInHours) {
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
