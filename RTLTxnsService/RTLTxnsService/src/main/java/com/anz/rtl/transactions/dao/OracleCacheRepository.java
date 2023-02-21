package com.anz.rtl.transactions.dao;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.anz.rtl.transactions.response.TransactionsResponse;
import com.anz.rtl.transactions.util.JsonUtil;
import com.anz.rtl.transactions.util.RetailOracleCachePartitionKeyUtil;

@Repository
@Transactional
public class OracleCacheRepository {

	private static final String SQL_INSERT_CACHE = "INSERT INTO BTR_OWNER.RETAIL_CACHE(RC_SRC_NM, RC_PART_KEY, RC_KEY, RC_TXN_CNT, RC_LAST_UPD_DT, RC_TXN_JSON_FILE) "
			+ "VALUES(:RC_SRC_NM, :RC_PART_KEY, :RC_KEY, :RC_TXN_CNT, :RC_LAST_UPD_DT, :RC_TXN_JSON_FILE)";

	private static final String SQL_SELECT_CACHE = "SELECT RC_TXN_CNT, RC_LAST_UPD_DT, RC_TXN_JSON_FILE FROM BTR_OWNER.RETAIL_CACHE "
			+ "WHERE RC_SRC_NM = :RC_SRC_NM AND RC_PART_KEY=:RC_PART_KEY AND RC_KEY=:RC_KEY ORDER BY RC_LAST_UPD_DT DESC ";

	private static final String DEFAULT_PARTITION = "DEF";

	@Autowired
	private RetailOracleCachePartitionKeyUtil cachePartitionKeyUtil;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Value("${spring.redis.key.expiretime:30}")
	private int expireTime;

	public void saveTransactions(TransactionsResponse response, String cacheKey, String appSource) {
		MapSqlParameterSource param = new MapSqlParameterSource();
		String partKey = cachePartitionKeyUtil.getPartionKey(appSource);
		param.addValue("RC_SRC_NM", appSource);
		param.addValue("RC_PART_KEY", partKey == null ? DEFAULT_PARTITION : partKey);
		param.addValue("RC_KEY", cacheKey);
		param.addValue("RC_TXN_CNT", response.getTransactions() != null ? response.getTransactions().size() : 0);
		param.addValue("RC_LAST_UPD_DT", Timestamp.from(Instant.now()));
		param.addValue("RC_TXN_JSON_FILE", new JsonUtil().marshal(response));
		jdbcTemplate.update(SQL_INSERT_CACHE, param);
	}

	public Map<String, TransactionsResponse> getTransactionForKey(String cacheKey, String accId, String appSource) {

		MapSqlParameterSource param = new MapSqlParameterSource();
		String partKey = cachePartitionKeyUtil.getPartionKey(appSource);
		param.addValue("RC_SRC_NM", appSource);
		param.addValue("RC_PART_KEY", partKey == null ? DEFAULT_PARTITION : partKey);
		param.addValue("RC_KEY", cacheKey);

		return jdbcTemplate.query(SQL_SELECT_CACHE, param, new ResultSetExtractor<Map<String, TransactionsResponse>>() {

			@Override
			public Map<String, TransactionsResponse> extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				Map<String, TransactionsResponse> response = new HashMap<>();
				TransactionsResponse txns = new TransactionsResponse();
				if (rs.next()) {
					boolean isCacheExpired = checkExpiry(rs.getTimestamp("RC_LAST_UPD_DT"));

					if (!isCacheExpired) {
						String jsonResponse = rs.getString("RC_TXN_JSON_FILE");
						txns = new JsonUtil().unmarshall(jsonResponse);
						response.put(accId, txns);
					}
				}
				return response;
			}
		});
	}

	private boolean checkExpiry(Date lastUpdatedDate) {
		LocalDateTime today = LocalDateTime.now();
		Date timeNow = convertToDateViaSqlTimestamp(today);

		// Calculate long value for a 30 mins duration
		long maxTimeExpiryDuration = MILLISECONDS.convert(expireTime, MINUTES);

		long duration = timeNow.getTime() - lastUpdatedDate.getTime();

		// If duration exceeds max duration(30 mins) then its expired.
		return duration > maxTimeExpiryDuration;
	}

	private Date convertToDateViaSqlTimestamp(LocalDateTime dateToConvert) {
		return java.sql.Timestamp.valueOf(dateToConvert);
	}
}
