package com.starcut.auth.sms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;

@Configuration
public class AwsConfiguration {

	@Value("${cloud.aws.credentials.accessKey}")
	private String accessKey;
	
	@Value("${cloud.aws.credentials.secretKey}")
	private String secretKey;
	
	@Value("${cloud.aws.region}")
	private String region;
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Bean
	public AmazonSNS snsClient() {
		if (accessKey != null && !accessKey.isEmpty()) {
			LOGGER.info("Using credentials for AWS SNS (SMS provider)");
			AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
			return AmazonSNSClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(region).build();
		}
		LOGGER.info("Using default client for AWS SNS (SMS provider)");
		return AmazonSNSClientBuilder.defaultClient();
	}
}
