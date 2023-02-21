package com.anz.rtl.transactions.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;

/**
 * Retail PartitionKey CacheBuilder
 * 
 * @author ANZ
 *
 */
@Component
public class RetailPartitionCacheUtil implements ApplicationListener<ApplicationReadyEvent> {

	/** LOGGER */
	private static final Logger LOG = LoggerFactory.getLogger(RetailPartitionCacheUtil.class);

	/** JDBC template */
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	public static final String RETAIL_PARTITION_KEY = "SELECT RPKR_COMP_ID,RPKR_PRD_CD,RPKR_ST_VAL,RPKR_END_VAL,RPKR_PART_KEY FROM RTL_PART_KEY_RANGE WHERE RPKR_PRD_CD = :prdCd ORDER BY RPKR_PART_KEY";
	public static final String PRD_CD_PARAM = "prdCd";
	public static final String RPKR_ST_VAL = "RPKR_ST_VAL";
	public static final String RPKR_END_VAL = "RPKR_END_VAL";
	public static final String RPKR_PART_KEY = "RPKR_PART_KEY";

	/**
	 * Retail Partition CacheBuilder
	 */
	private LoadingCache<String, Optional<Map<Range<Long>, Integer>>> retailPartitionCache = CacheBuilder.newBuilder()
			.maximumSize(1000).expireAfterAccess(24, TimeUnit.HOURS).recordStats()
			.build(new CacheLoader<String, Optional<Map<Range<Long>, Integer>>>() {
				@Override
				public Optional<Map<Range<Long>, Integer>> load(String prdCode) {
					Map<String, Object> parameters = new HashMap<>();
					Map<Range<Long>, Integer> entries = new HashMap<>();
					parameters.put(PRD_CD_PARAM, prdCode);
					long startTs = System.currentTimeMillis();
					LOG.debug("RetailPartitionCacheUtil SQL Query:{}, SQL Query Parameters:{}", RETAIL_PARTITION_KEY,
							Arrays.asList(parameters));
					return Optional.ofNullable(jdbcTemplate.query(RETAIL_PARTITION_KEY, parameters,
							new ResultSetExtractor<Map<Range<Long>, Integer>>() {
								public Map<Range<Long>, Integer> extractData(ResultSet rs) throws SQLException {
									while (rs.next()) {
										entries.put(Range.between(rs.getLong(RPKR_ST_VAL), rs.getLong(RPKR_END_VAL)),
												rs.getInt(RPKR_PART_KEY));
									}
									LOG.info("RetailPartitionCache : Product Code :{}, LoadTime :{}", prdCode,
											(System.currentTimeMillis() - startTs));
									return entries;
								}
							}));
				}
			});

	/**
	 * Get PartitionKey based on ProductCode and AccountNumber
	 * 
	 * @param prdCode
	 * @param accountNo
	 * @return PartitionKey
	 */
	public Integer getParttionKey(String prdCode, String accountNo, String acctType) {
		long accNoInt;
		if (acctType.contains("PC")) {
			accNoInt = Long.parseLong(accountNo.substring(0, 8));
			prdCode="PC";
		} else {
			accNoInt = Long.parseLong(accountNo);
		}
		try {
			Optional<Map<Range<Long>, Integer>> rangeKeys = retailPartitionCache.get(prdCode);
			if (rangeKeys.isPresent()) {
				return rangeKeys.get().entrySet().stream().filter(e -> e.getKey().contains(accNoInt))
						.map(Map.Entry::getValue).findFirst().orElse(0);
			}
		} catch (ExecutionException exe) {
			LOG.error("RetailPartition Cache Exception ", exe);
		}
		return 0;
	}

	/**
	 * Get CacheStats
	 * 
	 * @return
	 */
	public CacheStats getCacheStats() {
		return retailPartitionCache.stats();
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		retailPartitionCache.refresh("DDA");
		retailPartitionCache.refresh("CDA");
		retailPartitionCache.refresh("RSV");
		retailPartitionCache.refresh("ILS");
		retailPartitionCache.refresh("PC");
		retailPartitionCache.refresh("FM");
		LOG.debug("Retail Partition Cache stats - {}", retailPartitionCache.stats());
	}
}