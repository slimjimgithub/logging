package com.anz.rtl.transactions.util;

import java.util.Calendar;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@RunWith(MockitoJUnitRunner.class)
public class RetailPartitionCacheUtilTest {
	private static final Logger LOG = LoggerFactory.getLogger(RequestValidationTest.class);
	Calendar calendar = Calendar.getInstance();
	@Mock
	private NamedParameterJdbcTemplate jdbcTemplate;
	@InjectMocks
	RetailPartitionCacheUtil partitionCacheUtilTest;

	
	@Test
	public void testGetCacheStats() {
		partitionCacheUtilTest.getCacheStats();
	}

	@Test
	//@Ignore
	public void testOnApplicationEvent() {
		try {
			partitionCacheUtilTest.onApplicationEvent(null);
		} catch (Exception e) {

		}

	}
	
	@Test
	public void testgetParttionKeyForacctypePc() {
		String acctType="PC";
		String accountNo="4072200015623730";
		String prdCode="207476141";
		try {
			partitionCacheUtilTest.getParttionKey(prdCode, accountNo, acctType);
		}catch(Exception e) {
			
		}
	}
	
	@Test
	public void testgetParttionKeyForacctypenotPC() {
		String acctType="CAD";
		String accountNo="4072200015623730";
		String prdCode="PC";
		try {
			partitionCacheUtilTest.getParttionKey(prdCode, accountNo, acctType);
			
		}catch(Exception e) {
			
		}
	}


}
