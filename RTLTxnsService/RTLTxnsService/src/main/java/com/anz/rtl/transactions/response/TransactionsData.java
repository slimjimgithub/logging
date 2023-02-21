package com.anz.rtl.transactions.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import io.swagger.annotations.ApiModelProperty;

/**
 * Transactions Data Object Model
 *
 */

public class TransactionsData implements Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1059369740172336085L;

	@ApiModelProperty(value = "Post Date of the transaction in AU time zone. Date Format: YYYY-MM-DD\r\n"
			+ "This is applicable only for Prior day transactions\r\n", example = "2018-11-19", position = 1)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date postedDate;

	@ApiModelProperty(value = "Date and Time the transaction was received from originating customer"
			+ "\n This represents the Transaction Capture Date Time for Current day transactions"
			+ "\n This represents the Authorization Date Time for Outstanding transactions", example = "2019-11-13T08:37:57.147+11:00", position = 2)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Australia/Melbourne")
	private ZonedDateTime originDateTime;

	@ApiModelProperty(value = "Unique Identifier of the transaction", example = "CT1O000104191444, DDPQ681NJCM30001, 0005200910005313, 0120200840000007, 0121200650000001, ILPQ611NLKM00001, V21F476Z04", position = 4)
	private String transactionId;

	@ApiModelProperty(value = "Date from when the transaction will be in effect. Date Format: YYYY-MM-DD format", example = "2018-11-19", position = 5)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date valueDate;

	@ApiModelProperty(value = "This field represents the Transaction Status.\r\n" + "Valid Values:\r\n" + "POSTED \r\n"
			+ "PENDING \r\n" + "OUTSTANDING \r\n" + " \r\n" + "Prior Day Transactions will have POSTED status.\r\n"
			+ "Current day transactions will have PENDING status \r\n"
			+ "OUTSTANDING transactions will have OUTSTANDING status \r\n" + "", example = "POSTED", position = 6, required=true)
	private String status;

	@ApiModelProperty(value = "Identifier that represents the type of transaction. It's a two digit Numeric field.\n"
			+ "Only for Vision Plus Prior day transactions, this fields represents whether its Debit or Credit.", example = "13", position = 7)
	private String transactionType;

	@ApiModelProperty(value = "Source Transaction Code as sent by Core Systems ", example = "512", position = 8)
	private String transactionCode;

	@ApiModelProperty(value = "Transaction description and reference as given by the financial institution", example = "BILLED FINANCE CHARGES", position = 9)
	private String desc1;

	@ApiModelProperty(value = "Transaction description and reference as given by the financial institution", example = "BILLED FINANCE CHARGES", position = 10)
	private String desc2;

	@ApiModelProperty(value = "Transaction description and reference as given by the financial institution", example = "BILLED FINANCE CHARGES", position = 11)
	private String desc3;

	@ApiModelProperty(value = "Transaction description and reference as given by the financial institution", example = "BILLED FINANCE CHARGES", position = 12)
	private String desc4;

	@ApiModelProperty(value = "Transaction Amount \r\n"
			+ " For Operating Accounts, Credit Cards, Deposits and Loans:\r\n" + "No sign for Credit Transactions\r\n"
			+ "-ve sign for Debit Transactions \r\n" + " \r\n", example = "-4557", position = 13, required = true)
	private BigDecimal amount;

	@ApiModelProperty(value = "Balance of the account at the time the transaction was completed\r\n"
			+ "No sign for Positive Balance \r\n" + " -ve sign for Negative Balance \r\n"
			+ "", example = "-4557", position = 14)
	private BigDecimal runningBalance;

	@ApiModelProperty(value = "This field represents additional information about the transaction ", example = "EX125", position = 15)
	private String auxDom;

	@ApiModelProperty(value = "This field represents additional information about the transaction", example = "EX125", position = 16)
	private String exAuxDom;

	@ApiModelProperty(value = "This field represents the Cheque Serial number for Cheque transactions ", example = "000606063", position = 17)
	private Integer chequeSerialNum;

	@ApiModelProperty(value = "Reference number of a transaction. This is applicable only for Credit Cards.", example = "-4557", position = 18)
	private String referenceNum;

	@ApiModelProperty(value = "3 digit Identifier that represents the channel ", example = "801", position = 19)
	private String channelCode;

	@ApiModelProperty(value = "Merchant settlement unique identifier for  POS transactions", example = "GRCJXIAQISBSRXHRYKTF", position = 20)
	private String merchantSettlementId;

	@ApiModelProperty(value = "This field represents the card number of the card used for transaction. Card number will be masked in the below format based on the length of card number after trimming leading zeroes:\r\n" + 
			"15 digit - 4623 xxxx xxx1 415\r\n" + 
			"16 digit - 5501 xxxx xxxx 1516", example = "5501 xxxx xxxx 1516, 4623 xxxx xxx1 415", position = 21)
	private String cardUsed;

	@ApiModelProperty(value = "CardSequence number of the Card Used. This is applicable only for Credit Cards", example = "000606063", position = 22)
	private Integer cardSequence;

	@ApiModelProperty(value = "Transaction Amount in original currency for International transactions", example = "354435.545", position = 23)
	private String originalCurrencyAmount;

	@ApiModelProperty(value = "Conversion Fee for International transactions", example = "41.50  BBD", position = 24)
	private String conversionFee;

	@ApiModelProperty(value = "Foreign Exchange Rate Note: CIS will round CurRate to 4 decimal places", example = "12.34563", position = 22)
	private BigDecimal foreignExchangeRt;

	@ApiModelProperty(value = "Merchant Category Code of the Merchant involved in transaction", example = "0", position = 25)
	private String merchantCategoryCode;

	@ApiModelProperty(value = "This field represents the plan ", example = "10002", position = 26)
	private Integer planNum;

	@ApiModelProperty(value = "This field represents the plan sequence number ", example = "1", position = 27)
	private Integer planSeqNum;

	@ApiModelProperty(value = "Unique ID for NPP transactions", example = "123456", position = 28)
	private String puId;

	@ApiModelProperty(value = "Additional narrative for NPP transactions", example = "PTM1000000040502 CAP NPP load", position = 29)
	private String extendedNarrative;

	/*
	 * @ApiModelProperty(value = "P - Posted for Priorday & Blindspot \r\n" +
	 * "P - Pending for Intraday \r\n" + "O - Outstanding", example = "P", position
	 * = 29) private String transactionState;
	 */

	@ApiModelProperty(value = "This field represents the Payer name", example = "VAUGHN HENRY COTTON", position = 30)
	private String payer;

	@ApiModelProperty(value = "This field represents the Payee name ", example = "ANZ Verification", position = 31)
	private String payee;

	@ApiModelProperty(value = "This field represents the AliasID of NPP transaction", example = "anzverify1@gmail.com", position = 32)
	private String aliasId;

	@ApiModelProperty(value = "This field represents the Alias Name of NPP transaction", example = "ANZ NPP VERIFICATION ACCOUNT", position = 33)
	private String aliasName;

	@ApiModelProperty(value = "This field represents the End to End ID for NPP transaction", example = "ANZRBA.16", position = 34)
	private String customerRef;

	@ApiModelProperty(value = "This field Represents the Clearing Preference for NPP transaction", example = "FAST", position = 35)
	private String clearingPref;

	@ApiModelProperty(value = "This field Represents the Clearing subPreference for NPP transaction", example = "ICS1", position = 36)
	private String clearingSubPref;

	@ApiModelProperty(value = "This represents the Clearing Method for NPP transaction", example = "FAST", position = 37)
	private String clearingMethod;

	@ApiModelProperty(value = "This represents the Clearing SubMethod for NPP transaction", example = "ICS1", position = 38)
	private String clearingSubMethod;

	@ApiModelProperty(value = "This represents the reason code for NPP return transactions.", example = "FOCR", position = 39)
	private String reason;

	@ApiModelProperty(value = "This represents the Beneficiary's BSB for Outward transactions", example = "025896", position = 40)
	private String payeeBSB;

	@ApiModelProperty(value = "This represents the Beneficiary Account Number for Outward transactions", example = "985624561", position = 41)
	private String payeeAccNbr;

	@ApiModelProperty(value = "BPay Detail data object model", required = false, position = 42)
	// @JsonInclude(JsonInclude.Include.ALWAYS)
	private BPayDetails bpayDetails;

	@JsonIgnore
	@ApiModelProperty(hidden = true)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date sortingDate;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private int sortingTransStatus;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private int seqNo;
	
	@JsonIgnore
	@ApiModelProperty(hidden=true)
	private String cal;

	@ApiModelProperty(value = "This field represents source of authorisation transaction.", example = "POS, ATM, or VPL", position = 43)
	private String authorisationSource;

	@ApiModelProperty(value = "This field represents merchant name.", example = "COLES", position = 44)
	private String merchantName;

	@ApiModelProperty(value = "This field represents the Merchant Country in ISO format for international POS transactions.", example = "US", position = 46)
	private String merchantCountry;

	@ApiModelProperty(value = "This field represents Transaction Enrichment id related to LWC.", example = "df49cd3e-11bc-4e85-a63c-b1e680e1xEkk", position = 47)
	private String enrichmentID;

	// From DB
	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private String txnEnrichmentID;

	@ApiModelProperty(value = "This represents the Sender's BSB who initiated the transfer for Inward transactions", position = 48)
	private String payerBSB;

	@ApiModelProperty(value = "This represents the Sender's Account Number who initiated the transfer for Inward transactions", position = 49)
	private String payerAccNbr;

	@ApiModelProperty(value = " Represents the type of fee charged. Applicable only for CARD & ATM transactions", example = "OVERSEAS TRANSACTION FEE, OPERATOR FEE", position = 50)
	private String feeTyp;

	@ApiModelProperty(value = " Represents the entity who charges the fee.\r\n"
			+ "Valid Values: ANZ, NON-ANZ, SHARED", example = "ANZ", position = 51)
	private String feeChargedBy;

	@ApiModelProperty(value = "Represents the type of transaction based on Channel or payment method", example = " CARD, ATM, BPAY, DIRECT_DEBIT", position = 52)
	private String transactionCategory;
	
	@ApiModelProperty(value = "Represents the location group. Valid Values : INTERNATIONAL, DOMESTIC", example = "INTERNATIONAL", position = 53)
	private String locationGroup;
	
	@ApiModelProperty(value = "Represents the ATM type. Valid Values : ANZ, NON-ANZ ", example = "ANZ", position = 54)
	private String atmType;
	
	//This should be last on JSON
	@ApiModelProperty(value = "This field represents additional merchant details based on transaction identifier.", position = 55)
	private AdditionalMerchantDetails additionalMerchantInfo;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private String statementDetails;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private String emi;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private String posEnv;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private String obTrnType;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private String bpayCal;

	public String getBpayCal() {
		return bpayCal;
	}

	public void setBpayCal(String bpayCal) {
		this.bpayCal = bpayCal;
	}

	public String getFeeTyp() {
		return feeTyp;
	}

	public void setFeeTyp(String feeTyp) {
		this.feeTyp = feeTyp;
	}

	public String getFeeChargedBy() {
		return feeChargedBy;
	}

	public void setFeeChargedBy(String feeChargedBy) {
		this.feeChargedBy = feeChargedBy;
	}

	public String getTransactionCategory() {
		return transactionCategory;
	}

	public void setTransactionCategory(String transactionCategory) {
		this.transactionCategory = transactionCategory;
	}

	public String getObTrnType() {
		return obTrnType;
	}

	public void setObTrnType(String obTrnType) {
		this.obTrnType = obTrnType;
	}

	public String getPosEnv() {
		return posEnv;
	}

	public void setPosEnv(String posEnv) {
		this.posEnv = posEnv;
	}

	public String getEmi() {
		return emi;
	}

	public void setEmi(String emi) {
		this.emi = emi;
	}

	public String getStatementDetails() {
		return statementDetails;
	}

	public void setStatementDetails(String statementDetails) {
		this.statementDetails = statementDetails;
	}

	public AdditionalMerchantDetails getAdditionalMerchantInfo() {
		return additionalMerchantInfo;
	}

	public void setAdditionalMerchantInfo(AdditionalMerchantDetails additionalMerchantInfo) {
		this.additionalMerchantInfo = additionalMerchantInfo;
	}

	public String getAuthorisationSource() {
		return authorisationSource;
	}

	public void setAuthorisationSource(String authorisationSource) {
		this.authorisationSource = authorisationSource;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public Date getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(Date postedDate) {
		this.postedDate = postedDate;
	}

	public ZonedDateTime getOriginDateTime() {
		return originDateTime;
	}

	public void setOriginDateTime(ZonedDateTime originDateTime) {
		this.originDateTime = originDateTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public Date getValueDate() {
		return valueDate;
	}

	public void setValueDate(Date valueDate) {
		this.valueDate = valueDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getTransactionCode() {
		return transactionCode;
	}

	public BigDecimal getForeignExchangeRt() {
		return foreignExchangeRt;
	}

	public void setForeignExchangeRt(BigDecimal foreignExchangeRt) {
		this.foreignExchangeRt = foreignExchangeRt;
	}

	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}

	public String getDesc1() {
		return desc1;
	}

	public void setDesc1(String desc1) {
		this.desc1 = desc1;
	}

	public String getDesc2() {
		return desc2;
	}

	public void setDesc2(String desc2) {
		this.desc2 = desc2;
	}

	public String getDesc3() {
		return desc3;
	}

	public void setDesc3(String desc3) {
		this.desc3 = desc3;
	}

	public String getDesc4() {
		return desc4;
	}

	public void setDesc4(String desc4) {
		this.desc4 = desc4;
	}

	public BigDecimal getRunningBalance() {
		return runningBalance;
	}

	public void setRunningBalance(BigDecimal runningBalance) {
		this.runningBalance = runningBalance;
	}

	public String getAuxDom() {
		return auxDom;
	}

	public void setAuxDom(String auxDom) {
		this.auxDom = auxDom;
	}

	public String getExAuxDom() {
		return exAuxDom;
	}

	public void setExAuxDom(String exAuxDom) {
		this.exAuxDom = exAuxDom;
	}

	public Integer getChequeSerialNum() {
		return chequeSerialNum;
	}

	public void setChequeSerialNum(Integer chequeSerialNum) {
		this.chequeSerialNum = chequeSerialNum;
	}

	public String getReferenceNum() {
		return referenceNum;
	}

	public void setReferenceNum(String referenceNum) {
		this.referenceNum = referenceNum;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	public String getMerchantSettlementId() {
		return merchantSettlementId;
	}

	public void setMerchantSettlementId(String merchantSettlementId) {
		this.merchantSettlementId = merchantSettlementId;
	}

	public String getCardUsed() {
		return cardUsed;
	}

	public void setCardUsed(String cardUsed) {
		this.cardUsed = cardUsed;
	}

	public Integer getCardSequence() {
		return cardSequence;
	}

	public void setCardSequence(Integer cardSequence) {
		this.cardSequence = cardSequence;
	}

	public String getOriginalCurrencyAmount() {
		return originalCurrencyAmount;
	}

	public void setOriginalCurrencyAmount(String originalCurrencyAmount) {
		this.originalCurrencyAmount = originalCurrencyAmount;
	}

	public String getConversionFee() {
		return conversionFee;
	}

	public void setConversionFee(String conversionFee) {
		this.conversionFee = conversionFee;
	}

	public String getMerchantCategoryCode() {
		return merchantCategoryCode;
	}

	public void setMerchantCategoryCode(String merchantCategoryCode) {
		this.merchantCategoryCode = merchantCategoryCode;
	}

	public Integer getPlanNum() {
		return planNum;
	}

	public void setPlanNum(Integer planNum) {
		this.planNum = planNum;
	}

	public Integer getPlanSeqNum() {
		return planSeqNum;
	}

	public void setPlanSeqNum(Integer planSeqNum) {
		this.planSeqNum = planSeqNum;
	}

	public String getPuId() {
		return puId;
	}

	public void setPuId(String puId) {
		this.puId = puId;
	}

	public String getExtendedNarrative() {
		return extendedNarrative;
	}

	public void setExtendedNarrative(String extendedNarrative) {
		this.extendedNarrative = extendedNarrative;
	}

	public String getPayer() {
		return payer;
	}

	public void setPayer(String payer) {
		this.payer = payer;
	}

	public String getPayee() {
		return payee;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	public String getAliasId() {
		return aliasId;
	}

	public void setAliasId(String aliasId) {
		this.aliasId = aliasId;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public String getCustomerRef() {
		return customerRef;
	}

	public void setCustomerRef(String customerRef) {
		this.customerRef = customerRef;
	}

	public String getClearingPref() {
		return clearingPref;
	}

	public void setClearingPref(String clearingPref) {
		this.clearingPref = clearingPref;
	}

	public String getClearingSubPref() {
		return clearingSubPref;
	}

	public void setClearingSubPref(String clearingSubPref) {
		this.clearingSubPref = clearingSubPref;
	}

	public String getClearingMethod() {
		return clearingMethod;
	}

	public void setClearingMethod(String clearingMethod) {
		this.clearingMethod = clearingMethod;
	}

	public String getClearingSubMethod() {
		return clearingSubMethod;
	}

	public void setClearingSubMethod(String clearingSubMethod) {
		this.clearingSubMethod = clearingSubMethod;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@JsonIgnore
	public Date getSortingDate() {
		return sortingDate;
	}

	public void setSortingDate(Date sortingDate) {
		this.sortingDate = sortingDate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BPayDetails getBpayDetails() {
		return bpayDetails;
	}

	public void setBpayDetails(BPayDetails bpayDetails) {
		this.bpayDetails = bpayDetails;
	}

	public int getSortingTransStatus() {
		return sortingTransStatus;
	}

	public void setSortingTransStatus(int sortingTransStatus) {
		this.sortingTransStatus = sortingTransStatus;
	}

	public int getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}

	public String getPayeeBSB() {
		return payeeBSB;
	}

	public void setPayeeBSB(String payeeBSB) {
		this.payeeBSB = payeeBSB;
	}

	public String getPayeeAccNbr() {
		return payeeAccNbr;
	}

	public void setPayeeAccNbr(String payeeAccNbr) {
		this.payeeAccNbr = payeeAccNbr;
	}

	public String getCal() {
		return cal;
	}

	public void setCal(String cal) {
		this.cal = cal;
	}

	public String getMerchantCountry() {
		return merchantCountry;
	}

	public void setMerchantCountry(String merchantCountry) {
		this.merchantCountry = merchantCountry;
	}

	public String getEnrichmentID() {
		return enrichmentID;
	}

	public void setEnrichmentID(String enrichmentID) {
		this.enrichmentID = enrichmentID;
	}

	public String getTxnEnrichmentID() {
		return txnEnrichmentID;
	}

	public void setTxnEnrichmentID(String txnEnrichmentID) {
		this.txnEnrichmentID = txnEnrichmentID;
	}

	public String getPayerBSB() {
		return payerBSB;
	}

	public void setPayerBSB(String payerBSB) {
		this.payerBSB = payerBSB;
	}

	public String getPayerAccNbr() {
		return payerAccNbr;
	}

	public void setPayerAccNbr(String payerAccNbr) {
		this.payerAccNbr = payerAccNbr;
	}
	
	public String getLocationGroup() {
		return locationGroup;
	}

	public void setLocationGroup(String locationGroup) {
		this.locationGroup = locationGroup;
	}

	public String getAtmType() {
		return atmType;
	}

	public void setAtmType(String atmType) {
		this.atmType = atmType;
	}

	@Override
	public String toString() {
		return "TransactionsData [postedDate=" + postedDate + ", originDateTime=" + originDateTime + ", status="
				+ status + ", sortingDate=" + sortingDate + ", sortingTransStatus=" + sortingTransStatus + ", seqNo="
				+ seqNo + "]";
	}

	/*
	 * @Override public int compareTo(TransactionsData trans) { int ret=0;
	 * if(this.sortingDate.after(trans.getSortingDate())) { ret = 1; }
	 * if(this.sortingDate.before(trans.sortingDate)) { ret = -1; }else { ret = 0; }
	 * return ret; }
	 */

}
