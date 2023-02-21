package com.anz.rtl.transactions.request;
import javax.validation.Valid;
import org.springframework.stereotype.Component;
import io.swagger.annotations.ApiModelProperty;

@Component
public class EnrichTransactionRequest {
	@ApiModelProperty(value = "List of CALs should be passed in request", required = true, position = 2)
	@Valid
	private CalInfo calInfo;

	public CalInfo getCalInfo() {
		return calInfo;
	}

	public void setCalInfo(CalInfo calInfo) {
		this.calInfo = calInfo;
	}

}
