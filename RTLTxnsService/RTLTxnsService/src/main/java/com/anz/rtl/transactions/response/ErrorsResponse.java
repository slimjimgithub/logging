package com.anz.rtl.transactions.response;

import java.time.ZonedDateTime;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "Errors", description = "Errors object model")
public class ErrorsResponse {
	/** Error detail */
	private Error error;

	/**
	 * Error Response
	 * 
	 * @param error
	 */
	public ErrorsResponse(Error error) {
		this.error = error;
	}

	/**
	 * Error Response
	 * 
	 * @param error
	 * @return
	 */
	public static ErrorsResponse valueOf(Error error) {
		return new ErrorsResponse(error);
	}

	/**
	 * Error Response
	 * 
	 * @param code
	 * @param providerCode
	 * @param message
	 * @param detail
	 * @param serverDateTime
	 * @param httpStatus
	 * @return
	 */
	public static ErrorsResponse faultResponseOf(int code, String message, String detail,
			ZonedDateTime serverDateTime) {
		return new ErrorsResponse(Error.valueOf(code, message, detail, serverDateTime ));
	}

	/**
	 * @return the error
	 */
	public Error getFault() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setFault(Error error) {
		this.error = error;
	}}