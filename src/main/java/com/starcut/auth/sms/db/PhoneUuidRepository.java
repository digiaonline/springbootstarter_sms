package com.starcut.auth.sms.db;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starcut.auth.sms.db.entity.PhoneUuid;

public interface PhoneUuidRepository extends JpaRepository<PhoneUuid, String> {

}
