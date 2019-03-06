//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.db;

import com.starcut.auth.sms.db.entity.SmsLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
    @Modifying
    @Query("update SmsLog log set log.message = ?2, log.senderId = ?3, log.requestId = ?4 where log.topicArn = ?1")
    void recordPublication(String var1, String var2, String var3, String var4);

    Optional<SmsLog> findByRequestId(String var1);

    Long countByTopicArn(String var1);
}
