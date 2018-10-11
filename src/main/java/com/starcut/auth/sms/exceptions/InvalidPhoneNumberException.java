package com.starcut.auth.sms.exceptions;

public class InvalidPhoneNumberException extends SmsAuthException {

	public InvalidPhoneNumberException() {
	}

	public InvalidPhoneNumberException(String message) {
		super(message);
	}

	private static final long serialVersionUID = -7110496717863091711L;

}
