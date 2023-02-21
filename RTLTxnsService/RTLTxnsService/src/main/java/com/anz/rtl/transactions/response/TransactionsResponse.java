package com.anz.rtl.transactions.response;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.anz.rtl.transactions.request.TransactionAccountDetailRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import io.swagger.annotations.ApiModelProperty;

@Component
public class TransactionsResponse implements Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -7897811328033801930L;

	@ApiModelProperty(value = "Service Status ", required = true, position = 1)
	private Status status;

	@ApiModelProperty(value = "Request Identifier received from channel in UIID format. ", example = "47853b3a-32bf-fd0f-d146-0f051af77ff9", required = true, position = 2)
	private String requestId;

	@ApiModelProperty(value = "The time the response has prepared ", example = "2018-11-19T18:35:03Z", required = true, position = 3)
	@JsonFormat(timezone="yyyy-MM-dd'T'HH:mm:ssXXX")
	private ZonedDateTime responseDate;

	@ApiModelProperty(value = "Account info ", required = true, position = 4)
	private TransactionAccountDetailRequest accountInfo;

	@ApiModelProperty(value = "Transaction data object model", required = true, position = 5)
	private List<TransactionsData> transactions;

	@ApiModelProperty(value = "Record control in response", required = true, position = 6)
	@JsonInclude(JsonInclude.Include.ALWAYS)
	@Autowired
	private RecordControlResponse controlRecord;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private long totalTimeTaken;
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public TransactionAccountDetailRequest getAccountInfo() {
		return accountInfo;
	}

	public void setAccountInfo(TransactionAccountDetailRequest accountInfo) {
		this.accountInfo = accountInfo;
	}

	public List<TransactionsData> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<TransactionsData> transactions) {
		this.transactions = transactions;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public ZonedDateTime getResponseDate() {
		return responseDate;
	}

	public void setResponseDate(ZonedDateTime responseDate) {
		this.responseDate = responseDate;
	}

	public RecordControlResponse getControlRecord() {
		return controlRecord;
	}

	public void setControlRecord(RecordControlResponse controlRecord) {
		this.controlRecord = controlRecord;
	}

	public long getTotalTimeTaken() {
		return totalTimeTaken;
	}

	public void setTotalTimeTaken(long totalTimeTaken) {
		this.totalTimeTaken = totalTimeTaken;
	}
	
}
