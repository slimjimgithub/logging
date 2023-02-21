package com.anz.rtl.transactions.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@RunWith(MockitoJUnitRunner.class)
public class RetailOracleCachePartitionKeyUtilTest {
	private static final Logger LOG = LoggerFactory.getLogger(RetailOracleCachePartitionKeyUtilTest.class);
	@Mock
	private NamedParameterJdbcTemplate jdbcTemplate;

	@InjectMocks
	RetailOracleCachePartitionKeyUtil retailOracleCachePartitionKeyUtil;

	@Test
	public void testGetPartionKey() {
		String appSrc = "THA";
		try {
			retailOracleCachePartitionKeyUtil.getPartionKey(appSrc);
		} catch (Exception e) {

		}
	}

	@Test
	public void testGetCacheStats() {
		retailOracleCachePartitionKeyUtil.getCacheStats();
	}

	@Test
	public void testOnApplicationEvent() {
		retailOracleCachePartitionKeyUtil.onApplicationEvent(null);
	}


}
