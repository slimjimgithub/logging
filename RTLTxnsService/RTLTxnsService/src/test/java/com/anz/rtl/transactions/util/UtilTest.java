package com.anz.rtl.transactions.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UtilTest {
	@InjectMocks
	Util util;

	@Mock
	Statement statement;

	@Mock
	Connection con;

	@Test
	public void testIntToString() {
		int i = 10;
		Util.intToString(i);

	}

	
	@Test
	public void testBigDecimalToString() {
		BigDecimal i = BigDecimal.valueOf(21);
		Util.bigDecimalToString(i);

	}
	 
	@Test
	public void testRollbackDBTransaction() {
		Connection obj = null;
		util.rollbackDBTransaction(obj);
	}
	@Test
	public void testCloseDBresourceNull() {
		Statement obj = null;
		util.closeDBresource(obj);
	}

	@Test
	public void testCloseDBresource() {

		util.closeDBresource(statement);
	}

	@Test
	public void testCloseDBresourceConObj() {

		util.closeDBresource(con);
	}

	@Test
	public void testcloseDBresource() {
		Connection obj = null;
		util.closeDBresource(obj);
	}

	@Test
	public void testRollbackDBTransactionConObj(){
		Util.rollbackDBTransaction(con);
	}

	@Test
	public void testTimeToString() {
		LocalTime time = LocalTime.now();
		Util.timeToString(time);
	}

	@Test
	public void testBooleanToStringForFalse() {
		boolean flag = false;
		Util.booleanToString(flag);

	}

	@Test
	public void testBooleanToStringForTrue() {
		boolean flag = true;
		Util.booleanToString(flag);

	}

	@Test
	public void testGetCursorProdType() {
		String acctType = "P";
		Util.getCursorProdType(acctType);
	}
	
	@Test
	public void testisBpayOrDirectDebit() {
		
		String str = " skjdfk";
		Util.isBpayOrDirectDebit(str );
	}
	@Test
	public void testisBpayOrDirectDebitfornull() {
		
		String str = null;
		Util.isBpayOrDirectDebit(str );
	}

}
