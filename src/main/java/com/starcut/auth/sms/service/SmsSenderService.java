package com.starcut.auth.sms.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.DeleteTopicResult;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.starcut.auth.sms.config.SmsAuthConfig;
import com.starcut.auth.sms.db.SmsLogRepository;
import com.starcut.auth.sms.db.entity.SmsLog;

@Service
public class SmsSenderService {

	@Autowired
	private SmsAuthConfig smsAuthConfig;

	@Autowired
	private SmsLogRepository smsLogRepository;

	private AmazonSNS amazonSNSClient = AmazonSNSClientBuilder.defaultClient();

	private Logger LOGGER = LoggerFactory.getLogger(SmsSenderService.class);

	private final String SMS_PROTOCOL = "sms";

	private Map<String, MessageAttributeValue> getSmsAttributes(String senderId) {

		Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();
		smsAttributes.put("AWS.SNS.SMS.SenderID",
				new MessageAttributeValue().withStringValue(senderId).withDataType("String"));
		smsAttributes.put("AWS.SNS.SMS.SMSType",
				new MessageAttributeValue().withStringValue("Transactional").withDataType("String"));
		return smsAttributes;
	}

	public void sendSms(String phoneNumber, String message) {
		sendSms(phoneNumber, message, smsAuthConfig.getSenderId());
	}

	public void sendSms(String phoneNumber, String message, String senderId) {
		PublishResult result = amazonSNSClient.publish(new PublishRequest().withMessage(message)
				.withPhoneNumber(phoneNumber).withMessageAttributes(getSmsAttributes(senderId)));
		SmsLog smsLog = new SmsLog();
		smsLog.setMessage(message);
		smsLog.setRequestId(result.getMessageId());
		smsLog.setPhoneNumber(phoneNumber);
		smsLog.setSenderId(senderId);
		smsLogRepository.save(smsLog);
		LOGGER.info("Sent an SMS to " + phoneNumber + ". MessageId is " + result.getMessageId() + " with message "
				+ message);
	}

	public String registerTopic() {
		String topicId = UUID.randomUUID().toString();
		CreateTopicRequest createTopic = new CreateTopicRequest(topicId);
		CreateTopicResult result = amazonSNSClient.createTopic(createTopic);
		LOGGER.info("Create topic request: " + amazonSNSClient.getCachedResponseMetadata(createTopic));
		LOGGER.info("Create topic result: " + result);
		return result.getTopicArn();
	}

	public boolean subscribeToTopic(String phoneNumber, String topicArn) {
		SubscribeRequest subscribe = new SubscribeRequest(topicArn, SMS_PROTOCOL, phoneNumber);
		SubscribeResult subscribeResult = amazonSNSClient.subscribe(subscribe);
		SmsLog smsLog = new SmsLog();
		smsLog.setPhoneNumber(phoneNumber);
		smsLog.setTopicArn(topicArn);
		smsLogRepository.save(smsLog);
		LOGGER.info("Subscribe request: " + amazonSNSClient.getCachedResponseMetadata(subscribe));
		LOGGER.info("Subscribe result: " + subscribeResult);
		return subscribeResult.getSdkHttpMetadata().getHttpStatusCode() == 200;
	}

	public boolean sendSMSToTopic(String topicArn, String sender, String message) {
		PublishResult result = amazonSNSClient.publish(new PublishRequest().withMessage(message).withTopicArn(topicArn)
				.withMessageAttributes(getSmsAttributes(sender)));
		LOGGER.info("Sent an SMS to topic '" + topicArn + "'. MessageId is " + result.getMessageId() + " with message "
				+ message);
		boolean success = result.getSdkHttpMetadata().getHttpStatusCode() == 200;
		if (success) {
			smsLogRepository.recordPublication(topicArn, message, sender);
		}
		return success;
	}

	public boolean deleteTopic(String topicArn) {
		DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(topicArn);
		DeleteTopicResult deleteTopicResult = amazonSNSClient.deleteTopic(deleteTopicRequest);
		LOGGER.info("Delete topic " + topicArn + " received HTTP status code "
				+ deleteTopicResult.getSdkHttpMetadata().getHttpStatusCode());
		return deleteTopicResult.getSdkHttpMetadata().getHttpStatusCode() == 200;
	}
}
