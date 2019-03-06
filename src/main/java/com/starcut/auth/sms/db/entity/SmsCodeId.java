//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.starcut.auth.sms.db.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;

@Embeddable
public class SmsCodeId implements Serializable {
    private static final long serialVersionUID = 2167611409531348896L;
    private String phonenumber;
    private String code;

    public SmsCodeId() {
    }

    public String getPhonenumber() {
        return this.phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean equals(Object arg0) {
        if (arg0 != null && arg0 instanceof SmsCodeId) {
            SmsCodeId other = (SmsCodeId)arg0;
            return this.phonenumber.equals(other.getPhonenumber()) && this.code.equals(other.getCode());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.phonenumber.hashCode() + this.getCode().hashCode() * 31;
    }
}
