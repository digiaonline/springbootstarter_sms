//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.config;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.starcut.auth.sms.service.SmsAuthService;
import com.starcut.auth.sms.service.SmsSenderService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableConfigurationProperties({SmsAuthProperties.class})
@ComponentScan
@EntityScan({"com.starcut.auth.sms.db"})
@EnableJpaRepositories({"com.starcut.auth.sms.db"})
public class SmsAuthAutoConfiguration {
    private static final int DEFAULT_MIN_TIME_BETWEEN_TWO_SMS_IN_SECONDS = 1;
    private static final int DEFAULT_MAX_SMS_PER_PERIOD = 5;
    private static final int DEFAULT_PERIOD_IN_MINUTES = 1440;
    private static final int DEFAULT_CODE_VALIDITY_IN_MINUTES = 10;
    private static final int DEFAULT_CODE_LENGTH = 6;
    private static final int DEFAULT_MAX_TRIALS_PER_CODE = 3;
    private static final int DEFAULT_TRANSFER_DELAY_IN_HOURS = 24;
    private static final String DEFAULT_REGION = "FI";
    private static final List<String> DEFAULT_ALLOWED_REGIONS = new ArrayList();
    private static final String DEFAULT_SENDER_ID = "Starcut";
    private static final String DEFAULT_EASTER_EGG_PHONE_NUMBER = "+358999999999";
    private static final String DEFAULT_EASTER_EGG_CODE = "123456";
    @Autowired
    private SmsAuthProperties smsAuthProperties;
    private PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public SmsAuthAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean
    public SmsAuthConfig authSmsConfig() {
        String shortCode = this.smsAuthProperties.getShortCode() == null ? "" : this.smsAuthProperties.getShortCode();
        Integer maxSmsPerPeriod = this.smsAuthProperties.getMaxSmsPerPeriod() == null ? 5 : this.smsAuthProperties.getMaxSmsPerPeriod();
        Integer periodInMinutes = this.smsAuthProperties.getPeriodInMinutes() == null ? 1440 : this.smsAuthProperties.getPeriodInMinutes();
        Integer codeLength = this.smsAuthProperties.getCodeLength() == null ? 6 : this.smsAuthProperties.getCodeLength();
        Integer codeValidityInMinutes = this.smsAuthProperties.getCodeValidityInMinutes() == null ? 10 : this.smsAuthProperties.getCodeValidityInMinutes();
        Integer maxTrialsPerCode = this.smsAuthProperties.getMaxTrialsPerCode() == null ? 3 : this.smsAuthProperties.getMaxTrialsPerCode();
        Integer transferDelayInHours = this.smsAuthProperties.getTransferDelayInHours() == null ? 24 : this.smsAuthProperties.getTransferDelayInHours();
        String region = this.smsAuthProperties.getRegion() == null ? "FI" : this.smsAuthProperties.getRegion();
        List<String> allowedRegions = this.smsAuthProperties.getAllowedRegions() == null ? DEFAULT_ALLOWED_REGIONS : Arrays.asList(this.smsAuthProperties.getAllowedRegions().split(","));
        String senderId = this.smsAuthProperties.getSenderId() == null ? "Starcut" : this.smsAuthProperties.getSenderId();
        Integer minTimeBetweenTwoSmsInSecond = this.smsAuthProperties.getMinTimeBetweenTwoSmsInSeconds() == null ? 1 : this.smsAuthProperties.getMinTimeBetweenTwoSmsInSeconds();
        String easterEggPhoneNumber = this.smsAuthProperties.getEasterEggPhoneNumber() == null ? "+358999999999" : this.smsAuthProperties.getEasterEggPhoneNumber();
        String easterEggCode = this.smsAuthProperties.getEasterEggCode() == null ? "123456" : this.smsAuthProperties.getEasterEggCode();
        SmsAuthConfig authSmsConfig = new SmsAuthConfig();
        authSmsConfig.setShortCode(shortCode);
        authSmsConfig.setMaxSmsPerPeriod(maxSmsPerPeriod);
        authSmsConfig.setPeriodInMinutes(periodInMinutes);
        authSmsConfig.setCodeLength(codeLength);
        authSmsConfig.setCodeValidityInMinutes(codeValidityInMinutes);
        authSmsConfig.setMaxTrialsPerCode(maxTrialsPerCode);
        authSmsConfig.setRegion(region);
        authSmsConfig.setAllowedRegion((List)allowedRegions.stream().map((code) -> {
            return this.phoneNumberUtil.getCountryCodeForRegion(code.trim());
        }).collect(Collectors.toList()));
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
