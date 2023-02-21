package com.anz.rtl.transactions.request;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class TransactionRequestParam {

	/*private Boolean debitFlag;
	private Boolean creditFlag;*/
	private String creditDebitFlag;
	private Date startDate;
	private Date endDate;
	private Integer intradayFlag;
	private Integer priordayFlag;
	private Integer outstandingFlag;
	private BigDecimal minAmount;
	private BigDecimal maxAmount;
	private LocalTime startTime;
	private LocalTime endTime;
	private String descOperator;
	private String descSearch;
	private Integer lowChequeNum;
	private Integer highChequeNum;
	private Integer recordLimit;
	private String cursor;
	private String origApp;
	private String requestId;
	private Date effDate;
	private Date initDateTime;
	private String operatorId;
	private Integer branchId;
	private String terminalId;
	private Date lastLoadDate;
	private Date nextLoadDate;
	private int partKey;
	private int sessionTimeOut;
	private Integer includeMerchantInfo;
	private String apiVersion;
	

	public int getSessionTimeOut() {
		return sessionTimeOut;
	}

	public void setSessionTimeOut(int sessionTimeOut) {
		this.sessionTimeOut = sessionTimeOut;
	}

	public String getCreditDebitFlag() {
		return creditDebitFlag;
	}

	public void setCreditDebitFlag(String creditDebitFlag) {
		this.creditDebitFlag = creditDebitFlag;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Integer getIntradayFlag() {
		return intradayFlag;
	}

	public void setIntradayFlag(Integer intradayFlag) {
		this.intradayFlag = intradayFlag;
	}

	public Integer getPriordayFlag() {
		return priordayFlag;
	}

	public void setPriordayFlag(Integer priordayFlag) {
		this.priordayFlag = priordayFlag;
	}

	public Integer getOutstandingFlag() {
		return outstandingFlag;
	}

	public void setOutstandingFlag(Integer outstandingFlag) {
		this.outstandingFlag = outstandingFlag;
	}

	public BigDecimal getMinAmount() {
		return minAmount;
	}

	public void setMinAmount(BigDecimal minAmount) {
		this.minAmount = minAmount;
	}

	public BigDecimal getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(BigDecimal maxAmount) {
		this.maxAmount = maxAmount;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public String getDescOperator() {
		return descOperator;
	}

	public void setDescOperator(String descOperator) {
		this.descOperator = descOperator;
	}

	public String getDescSearch() {
		return descSearch;
	}

	public void setDescSearch(String descSearch) {
		this.descSearch = descSearch;
	}

	public Integer getLowChequeNum() {
		return lowChequeNum;
	}

	public void setLowChequeNum(Integer lowChequeNum) {
		this.lowChequeNum = lowChequeNum;
	}

	public Integer getHighChequeNum() {
		return highChequeNum;
	}

	public void setHighChequeNum(Integer highChequeNum) {
		this.highChequeNum = highChequeNum;
	}

	public Integer getRecordLimit() {
		return recordLimit;
	}

	public void setRecordLimit(Integer recordLimit) {
		this.recordLimit = recordLimit;
	}

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}

	public String getOrigApp() {
		return origApp;
	}

	public void setOrigApp(String origApp) {
		this.origApp = origApp;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Date getEffDate() {
		return effDate;
	}

	public void setEffDate(Date effDate) {
		this.effDate = effDate;
	}

	public Date getInitDateTime() {
		return initDateTime;
	}

	public void setInitDateTime(Date initDateTime) {
		this.initDateTime = initDateTime;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public Integer getBranchId() {
		return branchId;
	}

	public void setBranchId(Integer branchId) {
		this.branchId = branchId;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public Date getLastLoadDate() {
		return lastLoadDate;
	}

	public void setLastLoadDate(Date lastLoadDate) {
		this.lastLoadDate = lastLoadDate;
	}

	public Date getNextLoadDate() {
		return nextLoadDate;
	}

	public void setNextLoadDate(Date nextLoadDate) {
		this.nextLoadDate = nextLoadDate;
	}

	public int getPartKey() {
		return partKey;
	}

	public void setPartKey(int partKey) {
		this.partKey = partKey;
	}

	public Integer getIncludeMerchantInfo() {
		return includeMerchantInfo;
	}

	public void setIncludeMerchantInfo(Integer includeMerchantInfo) {
		this.includeMerchantInfo = includeMerchantInfo;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@Override
	public String toString() {
		return "TransactionRequestParam [creditDebitFlag=" + creditDebitFlag + ", startDate=" + startDate + ", endDate="
				+ endDate + ", intradayFlag=" + intradayFlag + ", priordayFlag=" + priordayFlag + ", outstandingFlag="
				+ outstandingFlag + ", minAmount=" + minAmount + ", maxAmount=" + maxAmount + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", descOperator=" + descOperator + ", descSearch=" + descSearch
				+ ", lowChequeNum=" + lowChequeNum + ", highChequeNum=" + highChequeNum + ", recordLimit=" + recordLimit
				+ ", cursor=" + cursor + ", origApp=" + origApp + ", requestId=" + requestId + ", effDate=" + effDate
				+ ", initDateTime=" + initDateTime + ", operatorId=" + operatorId + ", branchId=" + branchId
				+ ", terminalId=" + terminalId + ", lastLoadDate=" + lastLoadDate + ", nextLoadDate=" + nextLoadDate
				+ ", partKey=" + partKey + ", sessionTimeOut=" + sessionTimeOut + ", includeMerchantInfo="
				+ includeMerchantInfo + "]";
	}

}
