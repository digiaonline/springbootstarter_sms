//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.db.entity;

import com.starcut.auth.sms.db.entity.type.SmsCodeType;
import java.beans.Transient;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class SmsCode {
    @EmbeddedId
    SmsCodeId id;
    @CreationTimestamp
    private Instant createdAt;
    @Column(
        insertable = false,
        updatable = false
    )
    private String phonenumber;
    private Integer trials = 0;
    @Column(
        insertable = false,
        updatable = false
    )
    private String code;
    private Boolean validated = false;
    private SmsCodeType type;

    public SmsCode() {
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhonenumber() {
        return this.phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public Integer getTrials() {
        return this.trials;
    }

    public void setTrials(Integer trials) {
        this.trials = trials;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public SmsCodeId getId() {
        return this.id;
    }

    public void setId(SmsCodeId id) {
        this.id = id;
    }

    public Boolean getValidated() {
        return this.validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public SmsCodeType getType() {
        return this.type;
    }

    public void setType(SmsCodeType type) {
        this.type = type;
    }

    @Transient
    public void incrementTrials() {
        this.trials = this.trials + 1;
    }
}
