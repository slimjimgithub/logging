package com.anz.rtl.transactions.request;

import javax.validation.Valid;

import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModelProperty;

@Component
public class TransactionsRequest {

	@ApiModelProperty(value = "Account detail should be passed in request", required = true, position = 2)
	@Valid
	private TransactionAccountDetailRequest accountInfo;

	public TransactionAccountDetailRequest getAccountInfo() {
		return accountInfo;
	}

	public void setAccountInfo(TransactionAccountDetailRequest accountInfo) {
		this.accountInfo = accountInfo;
	}

}
