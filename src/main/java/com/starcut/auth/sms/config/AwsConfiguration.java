//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfiguration {
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;
    @Value("${cloud.aws.region}")
    private String region;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public AwsConfiguration() {
    }

    @Bean
    public AmazonSNS snsClient() {
        if (this.accessKey != null && !this.accessKey.isEmpty()) {
            this.LOGGER.info("Using credentials for AWS SNS (SMS provider)");
            AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(this.accessKey, this.secretKey));
            return (AmazonSNS)((AmazonSNSClientBuilder)((AmazonSNSClientBuilder)AmazonSNSClientBuilder.standard().withCredentials(awsCredentialsProvider)).withRegion(this.region)).build();
        } else {
            this.LOGGER.info("Using default client for AWS SNS (SMS provider)");
            return (AmazonSNS)((AmazonSNSClientBuilder)AmazonSNSClientBuilder.standard().withRegion(this.region)).build();
        }
    }
}
