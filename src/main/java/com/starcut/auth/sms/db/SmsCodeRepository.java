package com.starcut.auth.sms.db;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SmsCodeRepository extends JpaRepository<SmsCode, Long> {

	public Collection<SmsCode> findAllByPhonenumber(String phonenumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	public List<SmsCode> findSmsCodeByPhonenumberAndCreatedAtGreaterThanOrderByCreatedAtDesc(String phonenumber,
			Instant oldest);

	public Optional<SmsCode> findByPhonenumberAndCode(String phonenumber, String code);
}
