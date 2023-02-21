package com.anz.rtl.transactions.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
public class RetailOracleCachePartitionKeyUtil implements ApplicationListener<ApplicationReadyEvent> {

	/** LOGGER */
	private static final Logger LOG = LoggerFactory.getLogger(RetailOracleCachePartitionKeyUtil.class);

	/** JDBC template */
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	public static final String RETAIL_PARTITION_KEY = "SELECT RCPR_SRC_NM,RCPR_PART_KEY FROM BTR_OWNER.RTL_CACHE_PART_KEYS";
	public static final String RCPR_SRC_NM = "RCPR_SRC_NM";
	public static final String RCPR_PART_KEY = "RCPR_PART_KEY";

	/**
	 * Retail Partition CacheBuilder
	 */
	private LoadingCache<String, Optional<Map<String, String>>> retailPartitionCache = CacheBuilder.newBuilder()
			.maximumSize(1000).expireAfterAccess(24, TimeUnit.HOURS).recordStats()
			.build(new CacheLoader<String, Optional<Map<String, String>>>() {
				@Override
				public Optional<Map<String, String>> load(String appSrc) {
					Map<String, String> entries = new HashMap<>();
					LOG.debug("RetailOracleCachePartitionKeyUtil SQL Query:{}", RETAIL_PARTITION_KEY);
					return Optional.ofNullable(
							jdbcTemplate.query(RETAIL_PARTITION_KEY, new ResultSetExtractor<Map<String, String>>() {
								public Map<String, String> extractData(ResultSet rs) throws SQLException {
									while (rs.next()) {
										entries.put(rs.getString(RCPR_SRC_NM), rs.getString(RCPR_PART_KEY));
									}
									return entries;
								}
							}));
				}
			});

	public String getPartionKey(String appSrc) {
		try {
			return retailPartitionCache.get("").get().get(appSrc);
		} catch (ExecutionException e) {
			LOG.error("ERROR: ", e);
		}
		return null;
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
		retailPartitionCache.refresh("");
		LOG.debug("Retail Partition Cache stats - {}", retailPartitionCache.stats());
	}
}