package com.anz.rtl.transactions.util;

public class RtlServiceQuery {
	
	public static final String BLACKOUTDATESQL="SELECT FM_NXT_SCH_DT, FM_NXT_SCH_DT -1 FROM FEED_MST WHERE FM_FEED_GRP=:prodType AND ROWNUM=1"; 

}
