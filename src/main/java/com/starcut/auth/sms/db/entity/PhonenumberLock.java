//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.db.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PhonenumberLock {
    @Id
    private String phonenumber;
    private Boolean locked = false;

    public PhonenumberLock() {
    }

    public PhonenumberLock(String phoneNumber) {
        this.phonenumber = phoneNumber;
    }

    public String getPhonenumber() {
        return this.phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public Boolean getLocked() {
        return this.locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }
}
