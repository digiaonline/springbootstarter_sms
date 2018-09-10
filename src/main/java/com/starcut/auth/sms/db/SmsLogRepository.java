package com.starcut.auth.sms.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.starcut.auth.sms.db.entity.SmsLog;

public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {

	@Query("update SmsLog set message = ?2, senderId = ?3 where topicArn = ?1")
	void recordPublication(String topicArn, String message, String sender);

}
