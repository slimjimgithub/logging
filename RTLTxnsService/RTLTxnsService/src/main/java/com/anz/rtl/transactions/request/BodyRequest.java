package com.anz.rtl.transactions.request;

import java.util.HashMap;
import java.util.Map;

public class BodyRequest {

	private String cal;
	private String bankAccountTransactionType;

	public String getCal() {
		return cal;
	}

	public void setCal(String cal) {
		this.cal = cal;
	}

	public String getBankAccountTransactionType() {
		return bankAccountTransactionType;
	}

	public void setBankAccountTransactionType(String bankAccountTransactionType) {
		this.bankAccountTransactionType = bankAccountTransactionType;
	}

}
