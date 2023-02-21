package com.anz.rtl.transactions.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.anz.rtl.transactions.util.ResponseConstants;
import com.anz.rtl.transactions.util.RtlServiceQuery;

@Repository
public class RtlTxnDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Map<String, Object> selectBlackOut(String prodType) throws SQLException {

        try {
            if (ResponseConstants.CARD_PROD_TYPE.equals(prodType)) {
                prodType = ResponseConstants.CURSOR_CARD_PROD_TYPE;
            }
            List<Map<String, Object>> row = jdbcTemplate.queryForList(RtlServiceQuery.BLACKOUTDATESQL,
                    new MapSqlParameterSource("prodType", prodType));
            if (row != null && row.size() >= 1) {// It will be always 1
                return row.get(0);
            }

        } catch (EmptyResultDataAccessException ex) {
            throw ex;
        }
        return null;
    }

}
