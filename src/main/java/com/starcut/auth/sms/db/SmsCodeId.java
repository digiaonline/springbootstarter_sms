package com.starcut.auth.sms.db;

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

}
