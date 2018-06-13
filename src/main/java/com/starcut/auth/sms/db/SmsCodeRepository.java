package com.starcut.auth.sms.db;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsCodeRepository extends JpaRepository<SmsCode, Long> {

	public Collection<SmsCode> findAllByPhonenumber(String phonenumber);
	
	public Optional<SmsCode> findByPhonenumberAndCode(String phonenumber, String code);
}
