package com.anz.rtl.transactions.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import io.swagger.annotations.ApiModelProperty;

public class AdditionalMerchantDetails implements Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 1059369755552336085L;

	@ApiModelProperty(value = "Merchant Primary Name", example = "TREND MICRO AUSTRALIA", position = 2)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private String merchantPrimaryName;

	@ApiModelProperty(value = "LWC Unique Identifier ", example = "d2d5718e-5369-41f8-bb98-a1020da38416", position = 3)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private String merchantId;

	@ApiModelProperty(value = "Information on the category which the merchant is assigned to", example = "SHOPPING", position = 4)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private String primaryCategory;

	// @JsonIgnore
	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private String cal;

	@ApiModelProperty(value = "SubUrb", example = "North Sydney", position = 5)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private String suburb;

	@ApiModelProperty(value = "State", example = "KFC EARLWO", position = 6)
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private String state;

	@JsonProperty(access = Access.WRITE_ONLY)
	@ApiModelProperty(hidden = true)
	private String txnEnrichmentId;

	@ApiModelProperty(value = "This field represents whether the Merchant category is considered sensitive", example = "True , False", position = 7)
	private Boolean isSensitive;

	@ApiModelProperty(value = "Merchant Primary Address", example = "Haymarket NSW 2000, Australia", position = 8)
	private String primaryAddress;

	@ApiModelProperty(value = "List of Other names by which Merchant is known by. This can be a single name or array of names.", example = "[\"The Book Depository Limited\",\r\n"
			+ "\"Book Depository Ltd\"]", position = 9)
	private List<String> alsoKnownAs;

	@ApiModelProperty(hidden = true)
	@JsonProperty(access = Access.WRITE_ONLY)
	private BPayDetails bPayDetails;

	public BPayDetails getbPayDetails() {
		return bPayDetails;
	}

	public void setbPayDetails(BPayDetails bPayDetails) {
		this.bPayDetails = bPayDetails;
	}

	public String getPrimaryAddress() {
		return primaryAddress;
	}

	public void setPrimaryAddress(String primaryAddress) {
		this.primaryAddress = primaryAddress;
	}

	public List<String> getAlsoKnownAs() {
		return alsoKnownAs;
	}

	public void setAlsoKnownAs(List<String> alsoKnownAs) {
		this.alsoKnownAs = alsoKnownAs;
	}

	public Boolean getIsSensitive() {
		return isSensitive;
	}

	public void setIsSensitive(Boolean isSensitive) {
		this.isSensitive = isSensitive;
	}

	@ApiModelProperty(value = "merchantLogo", position = 8)
	private MerchantLogo merchantLogo;

	public String getCal() {
		return cal;
	}

	public void setCal(String cal) {
		this.cal = cal;
	}

	public String getMerchantPrimaryName() {
		return merchantPrimaryName;
	}

	public void setMerchantPrimaryName(String merchantPrimaryName) {
		this.merchantPrimaryName = merchantPrimaryName;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getPrimaryCategory() {
		return primaryCategory;
	}

	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}

	public String getSuburb() {
		return suburb;
	}

	public void setSuburb(String suburb) {
		this.suburb = suburb;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTxnEnrichmentId() {
		return txnEnrichmentId;
	}

	public void setTxnEnrichmentId(String txnEnrichmentId) {
		this.txnEnrichmentId = txnEnrichmentId;
	}

	public MerchantLogo getMerchantLogo() {
		return merchantLogo;
	}

	public void setMerchantLogo(MerchantLogo merchantLogo) {
		this.merchantLogo = merchantLogo;
	}

}
