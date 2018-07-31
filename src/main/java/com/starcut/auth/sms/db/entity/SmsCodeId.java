package com.starcut.auth.sms.db.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class SmsCodeId implements Serializable{

	private static final long serialVersionUID = 2167611409531348896L;

	private String phonenumber;
	
	private String code;

	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null || !(arg0 instanceof SmsCodeId)) {
			return false;
		}
		SmsCodeId other = (SmsCodeId) arg0;
		return this.phonenumber.equals(other.getPhonenumber()) && this.code.equals(other.getCode());
	}

	@Override
	public int hashCode() {
		return this.phonenumber.hashCode() + this.getCode().hashCode() * 31;
	}

}
