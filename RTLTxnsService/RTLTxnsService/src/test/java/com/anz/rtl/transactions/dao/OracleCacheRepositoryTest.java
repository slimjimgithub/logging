package com.anz.rtl.transactions.dao;

import com.anz.rtl.transactions.response.TransactionsResponse;
import com.anz.rtl.transactions.util.RetailOracleCachePartitionKeyUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@RunWith(MockitoJUnitRunner.class)
public class OracleCacheRepositoryTest {

    @InjectMocks
    OracleCacheRepository oracleCacheRepository;

    @Mock
    NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    RetailOracleCachePartitionKeyUtil cachePartitionKeyUtil;

    @Test
    public void saveTransactions() {
        oracleCacheRepository.saveTransactions(new TransactionsResponse(), "Test", "");
    }

    @Test
    public void getTransactionForKey() {
           oracleCacheRepository.getTransactionForKey("TEST", "100", "Test");
    }
}