package com.anz.rtl.transactions.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

public class Status implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2003663132304889799L;

	@ApiModelProperty(value = "Status Message  ", example="success",required = true,position=3)	
	private String description;
	
	@ApiModelProperty(value = "Status Code  ", example="200",required = true, position=1)	
	private Integer statusCode;
	
	/*@ApiModelProperty(value = "Service Result Code, Possible values 417, 955, 401 and any other value retuned by core. \r\n" + 
			"Along with StatusCode, IB uses this for messages to be displayed on the screen.   ", example="401",required = true, position=1)	
	private Integer ServiceResultCode;*/

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public Status(String description, Integer statusCode) {
		super();
		this.description = description;
		this.statusCode = statusCode;
	}

	/*public Integer getServiceResultCode() {
		return ServiceResultCode;
	}

	public void setServiceResultCode(Integer serviceResultCode) {
		ServiceResultCode = serviceResultCode;
	}
	
	*/

}
