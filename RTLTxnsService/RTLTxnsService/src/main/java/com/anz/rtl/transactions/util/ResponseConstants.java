package com.anz.rtl.transactions.util;

public class ResponseConstants {

	public static final String XML_HDR_DEC = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	public static final String CARD_PROD_TYPE = "PC";
	public static final String CURSOR_CARD_PROD_TYPE = "VSN";
	public static final String V2P_PROD_TYPE = "FM";
	public static final String CURSOR_V2P_PROD_TYPE = "V2P";
	public static final String CURSOR_CAP_PROD_TYPE = "CAP";
	public static final String END_DATE_IS_BEFOR_TWO_YEARS = "End date is before two years from current date";

	public static final String START_DATE_IS_AFTER_TODAYS_DATE = "Current date is older than start date";
	
	public static final String START_DATE_IS_BIGGER_THAN_END_DATE = "End date is older than start date";
	
	public static final String END_DATE_IS_OLDER_THAN_TWO_YEARS_DATE = "End date is older than two years date";

	public static final String START_END_DATE_IS_BEFORE_TODAYS_DATE = "Start date and End date older than two yaers date";

	public static final String START_DATE_IS_AFTER_END_DATE = "End date is older than start date";

	public static final String START_AND_END_DATE_BOTH_ARE_EQUAL = "Start date and End date both than two yaers date";

	public static final String START_END_DATE_IS_AFTER_TODAYS_DATE = "Start date and End date are bigger than current date";
	
	public static final String INVALID_DATE_RANGE = "Invalid Date range";

	public static final String MINAMOUNT_IS_BIGGER_THAN_MAXAMOUNT = "Min amount is greater than max amount";
	
	public static final String AMOUNTFILTERNEGITIVE = "Amount filter cannot have a negative value";

	public static final String LOWCHEQUE_IS_BIGGER_THAN_HIGHCHEQUE = "Low cheque no is greater than high cheque no";
	
	public static final String CHEQUEFILTER_NOT_SUPPORTED = "Cheque filter is not supported for V+ accounts";
	
	public static final String END_TIME_CANNOT_BE_NULL = "End time cannot be null";
	
	public static final String START_TIME_CANNOT_BE_NULL = "Start time cannot be null";

	public static final String INVALID_VALUE_FOR_PRIORDAY_FLAG = "Invalid value for priorday flag. Valid values are ture and false";

	public static final String INVALID_VALUE_FOR_INTRADAY_FLAG = "Invalid value for intraday flag. Valid values are ture and false";

	public static final String INVALID_VALUE_FOR_OUTSTANDING_FLAG = "Invalid value for outstanding flag. Valid values are ture and false";

	public static final String INVALID_VALUE_FOR_DEBIT_FLAG = "Invalid value for debit flag. Valid values are ture and false";

	public static final String INVALID_VALUE_FOR_CREDIT_FLAG = "Invalid value for credit flag. Valid values are ture and false";
}
