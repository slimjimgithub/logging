package com.anz.rtl.transactions.request;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Component;



import io.swagger.annotations.ApiModelProperty;

@Component
public class CalInfo implements Serializable{

	private static final long serialVersionUID = -7545785506921721317L;
	
	@ApiModelProperty(value = "CAL info should be populated here ", required = false, example ="COLES 345",position = 1)
	private  List<BodyRequest> cal;

	public List<BodyRequest> getCal() {
		return cal;
	}

	public void setCal(List<BodyRequest> cal) {
		this.cal = cal;
	}


}