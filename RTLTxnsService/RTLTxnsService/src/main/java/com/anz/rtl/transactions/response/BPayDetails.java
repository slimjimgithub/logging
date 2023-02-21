package com.anz.rtl.transactions.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import io.swagger.annotations.ApiModelProperty;

public class BPayDetails {


	@ApiModelProperty(value = "BPay Biller Code for the transaction", example = "AnzApp", position = 1)
	@JsonProperty(access = Access.READ_ONLY)
	private String billerCode;

	@JsonProperty(value = "billerCode", access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private List<Integer> billerCodeList;
    
    @ApiModelProperty(value = "Name of the BPay biller for the transaction ", example = "ANZApp", position = 2)
    private String billerName;
    
    @ApiModelProperty(value = "BPay CRN for the transaction", example = "123456", position = 3)
    private String crn;
    
    @ApiModelProperty(value = "6 Digit APCA number for the initiating institution", example = "123456", position = 4)
    private String apcaNum;
    
    @ApiModelProperty(value = "Receipt Number of Single BPAY Debit transaction", example = "676389", position = 5)
    private String receiptNbr;

	public List<Integer> getBillerCodeList() {
		return billerCodeList;
	}

	public void setBillerCodeList(List<Integer> billerCodeList) {
		this.billerCodeList = billerCodeList;
	}

	public String getReceiptNbr() {
		return receiptNbr;
	}

	public void setReceiptNbr(String receiptNbr) {
		this.receiptNbr = receiptNbr;
	}

	public String getBillerCode() {
		return billerCode;
	}

	public void setBillerCode(String billerCode) {
		this.billerCode = billerCode;
	}

	public String getBillerName() {
		return billerName;
	}

	public void setBillerName(String billerName) {
		this.billerName = billerName;
	}

	public String getCrn() {
		return crn;
	}

	public void setCrn(String crn) {
		this.crn = crn;
	}

	public String getApcaNum() {
		return apcaNum;
	}

	public void setApcaNum(String apcaNum) {
		this.apcaNum = apcaNum;
	}
    
    
}
