//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.db;

import com.starcut.auth.sms.db.entity.PhonenumberLock;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface PhonenumberLockRepository extends JpaRepository<PhonenumberLock, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PhonenumberLock> findByPhonenumber(String var1);
}
