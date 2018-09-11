package com.starcut.auth.sms.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.starcut.auth.sms.db.entity.SmsLog;

public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {

	@Modifying
	@Query("update SmsLog log set log.message = ?2, log.senderId = ?3, log.requestId = ?4 where log.topicArn = ?1")
	void recordPublication(String topicArn, String message, String sender, String requestId);

}
