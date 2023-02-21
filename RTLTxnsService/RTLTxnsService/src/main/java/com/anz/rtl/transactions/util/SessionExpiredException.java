package com.anz.rtl.transactions.util;

public class SessionExpiredException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public SessionExpiredException(String exception) {
		super(exception);
	}
}
