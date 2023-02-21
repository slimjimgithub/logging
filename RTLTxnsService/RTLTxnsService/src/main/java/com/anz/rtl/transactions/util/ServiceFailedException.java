package com.anz.rtl.transactions.util;

public class ServiceFailedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ServiceFailedException(String exception) {
		super(exception);
	}
}
