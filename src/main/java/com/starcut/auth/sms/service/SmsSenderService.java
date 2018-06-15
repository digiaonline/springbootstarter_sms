package com.starcut.auth.sms.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.starcut.auth.sms.config.SmsAuthConfig;

public class SmsSenderService {

	@Autowired
	private SmsAuthConfig smsAuthConfig;
	
	private AmazonSNS amazonSNSClient = AmazonSNSClientBuilder.defaultClient();
	
	private Logger LOGGER = LoggerFactory.getLogger(SmsSenderService.class);
	
	private Map<String, MessageAttributeValue> getSmsAttributes() {
		
		Map<String, MessageAttributeValue> smsAttributes =
		        new HashMap<String, MessageAttributeValue>();
		smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
		        .withStringValue(smsAuthConfig.getSenderId())
		        .withDataType("String"));
		smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
		        .withStringValue("Transactional") //Sets the type to promotional.
		        .withDataType("String"));
		return smsAttributes;
	}
	
	public void sendSms(String phoneNumber, String message) {
		PublishResult result = amazonSNSClient.publish(new PublishRequest()
                .withMessage(message)
                .withPhoneNumber(phoneNumber)
                .withMessageAttributes(getSmsAttributes()));
		LOGGER.info("Sent an SMS to " + phoneNumber + ". MessageId is " + result.getMessageId());
	}
}
