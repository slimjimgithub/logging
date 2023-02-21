package com.anz.rtl.transactions.dao;

import com.anz.rtl.transactions.util.RtlServiceQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.SQLException;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RtlTxnDaoTest {

    @InjectMocks
    RtlTxnDao rtlTxnDao;

    @Mock
    NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void selectBlackOut() throws SQLException {
           rtlTxnDao.selectBlackOut("PC");
    }
}