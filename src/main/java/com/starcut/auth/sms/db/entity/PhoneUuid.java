//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.db.entity;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Entity
public class PhoneUuid {
    @Id
    private String phoneNumber;
    @NotBlank
    private String uuid;
    private String newUuid;
    private Instant changeRequestedAt;

    public PhoneUuid() {
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNewUuid() {
        return this.newUuid;
    }

    public void setNewUuid(String newUuid) {
        this.newUuid = newUuid;
    }

    public Instant getChangeRequestedAt() {
        return this.changeRequestedAt;
    }

    public void setChangeRequestedAt(Instant changeRequestedAt) {
        this.changeRequestedAt = changeRequestedAt;
    }
}
