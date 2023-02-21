package com.anz.rtl.transactions.util;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.anz.rtl.transactions.constants.TransactionConstants;


public class TransactionMdcLogger {

	/*
	 * CR-061: new MDC log
	 */
	private static final Logger MDC_LOG = LoggerFactory.getLogger("txnmdclog");
	private static final Logger LOG = LoggerFactory.getLogger(TransactionMdcLogger.class);


	public static void writeToMDCLog(String requestId, String status, Long timeTaken, String channel, String productType,
			String accountId, int recordSent, String requestCursor, String responseCursor, Date startDate, Date endDate){
    	try{
    		//MDC.clear();
			MDC.put(TransactionConstants.REQUEST_ID, (requestId == null ? "" : requestId));
			MDC.put(TransactionConstants.CHANNEL, (channel == null ? "" : channel));
			MDC.put(TransactionConstants.STATUS, (status == null ? "" : status));
			MDC.put(TransactionConstants.PRODUCT_TYPE, (productType == null ? "" : productType));
			if(productType != null && productType.startsWith("PC") && accountId != null && accountId.length() > 10) {
				MDC.put(TransactionConstants.ACCOUNT_ID, getMasked(accountId));
			} else {
				MDC.put(TransactionConstants.ACCOUNT_ID, (accountId == null ? "" : accountId));
			}
			MDC.put(TransactionConstants.RESPONSE_RECORD_SENT, Integer.toString(recordSent));
			MDC.put(TransactionConstants.REQUEST_CURSOR, (requestCursor == null ? "" : requestCursor));
			MDC.put(TransactionConstants.RESPONSE_CURSOR, (responseCursor == null ? "" : responseCursor));
			MDC.put(TransactionConstants.START_DATE, (startDate == null ? "" : Util.dateToString(startDate, "yyyy-MM-dd")));
			MDC.put(TransactionConstants.END_DATE, (endDate == null ? "" : Util.dateToString(endDate, "yyyy-MM-dd")));
			try {
				MDC.put(TransactionConstants.TOTAL_TIMETAKEN, String.valueOf(timeTaken));
			} catch(Exception e){
				MDC.put(TransactionConstants.TOTAL_TIMETAKEN, "");
			}
			MDC_LOG.info(TransactionConstants.SERVICE_NAME);
    	} catch(Throwable t) {
    		LOG.warn("Unable to update MDC log for request :" + requestId + "Exception : " + t.getMessage());
    	}
	}

	private static String getMasked(String toMask) {
		int startIndex = 6;
		int endIndex = toMask.length() - 4;
        StringBuilder sb = new StringBuilder();
        sb.append(toMask.substring(0, startIndex));
        for(int i=startIndex; i< endIndex; i++)
            sb.append('X');
        sb.append(toMask.substring(endIndex));
        return sb.toString();
    }

}
