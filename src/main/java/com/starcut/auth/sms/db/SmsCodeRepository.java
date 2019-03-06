//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.db;

import com.starcut.auth.sms.db.entity.SmsCode;
import com.starcut.auth.sms.db.entity.SmsCodeId;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsCodeRepository extends JpaRepository<SmsCode, SmsCodeId> {
    Collection<SmsCode> findAllByPhonenumber(String var1);

    List<SmsCode> findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(String var1, Instant var2);

    Optional<SmsCode> findByPhonenumberAndCode(String var1, String var2);
}
