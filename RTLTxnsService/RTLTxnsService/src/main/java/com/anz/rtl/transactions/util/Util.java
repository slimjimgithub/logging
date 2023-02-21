package com.anz.rtl.transactions.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.anz.rtl.transactions.constants.TransactionConstants;
import com.anz.rtl.transactions.service.RetailTransactionService;

public class Util {

	private static final Logger LOG = LoggerFactory.getLogger(RetailTransactionService.class);

	public static Boolean setFlags(int flag) {
		boolean strFlag = true;
		if (flag == 1) {
			strFlag = true;
		} else {
			strFlag = false;
		}
		return strFlag;
	}

	public static String dateToString(Date date, String format) {
		String strDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		if (date != null) {
			strDate = formatter.format(date);
		}

		// edDate = formatter.format(endDate);
		return strDate;
	}

	public static String timeToString(LocalTime time) {
		String parsedTime = null;
		if (time != null) {
			parsedTime = time.toString();
		}

		// edDate = formatter.format(endDate);
		return parsedTime;
	}

	public static String intToString(int i) {
		String str;
		str = Integer.toString(i);
		return str;
	}

	public static String bigDecimalToString(BigDecimal i) {
		String str;
		str = i.toString();
		return str;
	}

	public static String booleanToString(boolean flag) {

		String strFlag = "false";
		if (flag) {
			strFlag = "true";
		}
		return strFlag;

	}

	public static void rollbackDBTransaction(Connection obj) {
		if (obj != null) {
			try {
				obj.rollback();
			} catch (SQLException e) {
				LOG.warn("Error on rollback -" + e.getMessage());
			}
		}
	}

	public static void closeDBresource(Statement obj) {
		if (obj != null) {
			try {
				obj.close();
			} catch (SQLException e) {
				LOG.warn("Error on db resource release-" + e.getMessage());
			}
		}
	}

	public static void closeDBresource(Connection obj) {
		if (obj != null) {
			try {
				obj.close();
			} catch (SQLException e) {
				LOG.warn("Error on db resource release-" + e.getMessage());
			}
		}
	}

	public static String getCursorProdType(String acctType) {
		String ret = null;
		if (acctType == null || acctType.length() < 2) {
			return ret;
		}

		if (ResponseConstants.CARD_PROD_TYPE.equals(acctType.substring(0, 2))) {
			ret = ResponseConstants.CURSOR_CARD_PROD_TYPE;
		} else if (ResponseConstants.V2P_PROD_TYPE.equals(acctType.substring(0, 2))) {
			ret = ResponseConstants.CURSOR_V2P_PROD_TYPE;
		} else {
			ret = acctType;
		}

		return ret;
	}

	public static Date dateParse(String inputDate, String dateFormat) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		Date date = new Date();
		try {
			synchronized (formatter) {
				date = formatter.parse(inputDate);
			}
		} catch (ParseException e) {
			LOG.error("Error Occurred while parsing the Date" + e);
		}
		return date;

	}
	
	public static boolean isEmptyString(String str) {
		return StringUtils.isEmpty(str);
	}
	
	public static boolean isBpayOrDirectDebit(String str) {
		if(!isEmptyString(str)) {
			return (str.equalsIgnoreCase(TransactionConstants.BPAY) ||str.equalsIgnoreCase(TransactionConstants.DIRECTDEBIT));
		}
		return false;
	}
	
	

}
