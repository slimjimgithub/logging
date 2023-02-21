package com.anz.rtl.transactions.response;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Error", description = "Error object model")
public class Error {

	/** Error Code */
	@ApiModelProperty(example = "400", position = 1, required = true)
	private int code;

	/** Message */
	@ApiModelProperty(example = "Bad Request - Request failed", position = 2, required = true)
	private String message;

	/** Detailed Message */
	@ApiModelProperty(example = "Date format is not correct", position = 3, required = true)
	private String detail;

	/** Server TimeStamp */
	@JsonFormat(timezone="yyyy-MM-dd'T'HH:mm:ssXXX")
	//@JsonSerialize(using = DateTimeZoneSerializer.class)
	@ApiModelProperty(example = "1999-03-22T05:06:07.00+10:00", position = 4, required = true)
	private ZonedDateTime serverDateTime;

	/**
	 * @param i
	 * @param providerCode
	 * @param message
	 * @param detail
	 * @param serverDateTime
	 * @param httpStatus
	 */
	public Error(int i, String message, String detail, ZonedDateTime serverDateTime) {
		this.code = i;
		this.message = message;
		this.detail = detail;
		this.serverDateTime = serverDateTime;
	}

	/**
	 * Return Error Object
	 * 
	 * @param code
	 * @param providerCode
	 * @param message
	 * @param detail
	 * @param serverDateTime
	 * @param httpStatus
	 */
	public static Error valueOf(int code, String message, String detail, ZonedDateTime serverDateTime) {
		return new Error(code, message, detail, serverDateTime);
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * @param detail the detail to set
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	public ZonedDateTime getServerDateTime() {
		return serverDateTime;
	}

	public void setServerDateTime(ZonedDateTime serverDateTime) {
		this.serverDateTime = serverDateTime;
	}

		
}