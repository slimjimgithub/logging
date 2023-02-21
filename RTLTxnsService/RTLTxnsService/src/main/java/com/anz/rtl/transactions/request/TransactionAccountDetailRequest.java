package com.anz.rtl.transactions.request;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModelProperty;

@Component
public class TransactionAccountDetailRequest implements Serializable{

	private static final long serialVersionUID = -7545782205921721317L;

	@ApiModelProperty(value = "System Code of the account", required = false, example ="CAP",position = 1)
	//@NotEmpty(message="{System.code.null}")
	@Size(min=3, max=3, message="{system.code.size}")
	private String systemCode;
	
	@ApiModelProperty(value = "Country Code of the account in ISO format", required = false, example ="AU",position = 2)
	@Size(max=2, message="{country.code.size}")
	private String countryCode;
	
	@ApiModelProperty(value = "Currency code of the account in ISO format", required = false, example ="AUD",position = 3)
	@Size(max=3, message="{currency.code.size}")
	private String currencyCode;
	
	@ApiModelProperty(value = "3 digit code that defines a particular entity in MIDANZ", required = false, example ="AUA",position = 4)
	@Size(max=3, message="{book.code.size}")
	private String bookCode;
	
	@ApiModelProperty(value = "Unique identifier to identify the Account. \r\n" + 
			"This field holds the Account number for Operating Accounts, Deposits and Loan. \r\n" + 
			"This field holds the Card Number for Credit Cards. \r\n" + 
			"CIS will resolve the Account number from Card Number to retrieve the details from CIS DB\r\n" + 
			"", required = true, example ="0004567896435789",position = 5)
	@NotEmpty(message="Account Id cannot be null")
	@Size(max=23, message="{account.size}")
	private String accountId;

	@ApiModelProperty(value = "Unique identifier that represents the Product Code as received from core banking.\n"
			+ "Valid Values: DDA, CDA, RSV, ILS, PCB, PCP, PCV ", example ="CDA",required = true, position = 6)
	@NotEmpty(message="Product Type cannot be null")
	@Size(min=2, max=3, message="{product.type.size}")
	private String productType;
	
	@ApiModelProperty(value = "This field represents the Customer Reference Number(CRN) used in IB and ANZ APP Channels", required = false, example ="14353646475858",position = 7)
	//@NotEmpty(message="Customer Reference Number (CRN) cannot be null")
	@Size(max=23, message="{custPermId.size}")
	private String custPermId;

	public String getCustPermId() {
		return custPermId;
	}

	public void setCustPermId(String custPermId) {
		this.custPermId = custPermId;
	}

	public String getSystemCode() {
		return systemCode;
	}

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getBookCode() {
		return bookCode;
	}

	public void setBookCode(String bookCode) {
		this.bookCode = bookCode;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}
	
}
