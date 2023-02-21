package com.anz.rtl.transactions.response;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;

@Component
public class EnrichTransactionResponse {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -7897811555533801930L;

	private Status status;
	private String requestId;
	 @JsonFormat(timezone="yyyy-MM-dd'T'HH:mm:ssXXX")
	private Date responseDate;
	private List<AdditionalMerchantDetails> merchantData;


	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<AdditionalMerchantDetails> getMerchantData() {
		return merchantData;
	}

	public void setMerchantData(List<AdditionalMerchantDetails> merchantData) {
		this.merchantData = merchantData;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Date getResponseDate() {
		return responseDate;
	}

	public void setResponseDate(Date responseDate) {
		this.responseDate = responseDate;
	}

}
