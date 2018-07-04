package com.starcut.auth.sms.db;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.starcut.auth.sms.db.entity.PhonenumberLock;

public interface PhonenumberLockRepository extends JpaRepository<PhonenumberLock, String>{
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	public Optional<PhonenumberLock> findByPhonenumber(String phonenumber);
}
