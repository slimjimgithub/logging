/*package com.anz.rtl.transactions.util;

import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anz.rtl.transactions.request.TransactionsRequest;

public class ValidationProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(ValidationProcessor.class);

	private String validateRequest(final TransactionsRequest request) throws Exception {
		
		String validationStatus = "Success";
		String ret = null;
		
		// companyId validation
		ret = stringValidate.apply(Integer.toString(request.getInitCompany()), ResponseConstants.COMPID_VAL_ERR);
		if (ret != null) {
			return ret;
		}
		
		// operatorId validation
		ret = stringValidate.apply(request.getOperatorId(), ResponseConstants.COMPID_VAL_ERR);
		
		return validationStatus;
	}

	
	 * private static String validateString(String value, String errorCode) throws
	 * Exception { String ret = null;
	 * 
	 * if (value == null || value.trim().isEmpty()) { return errMsg; }
	 * 
	 * return ret; }
	 

	BiFunction<String, String, String> stringValidate = (value,
			errorCode) -> (value == null || value.trim().isEmpty()) ? errorCode : null;
}
*/