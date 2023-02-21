package com.anz.rtl.transactions.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.anz.rtl.transactions.dao.OracleCacheRepository;
import com.anz.rtl.transactions.dao.RedisTransactionRepository;
import com.anz.rtl.transactions.request.TransactionRequestParam;
import com.anz.rtl.transactions.request.TransactionsRequest;
import com.anz.rtl.transactions.response.TransactionsResponse;

@Service
public class CacheService {

	private static final Logger LOG = LoggerFactory.getLogger(CacheService.class);

	@Autowired
	private RedisTransactionRepository redisTransactionRepository;

	@Autowired
	private OracleCacheRepository oracleCacheRepository;

	private static final String CONST_Y = "Y";

	@Value(value = "${redis.enable:N}")
	private String isRedisEnabled;

	@Value("${oracle.cache.enable:N}")
	private String isOracleEnabled;

	public Map<String, TransactionsResponse> getTransaction(String cacheKey, String accId, String appSource) {
		Map<String, TransactionsResponse> response = null;
		try {
			if (CONST_Y.equalsIgnoreCase(isRedisEnabled))
				response = redisTransactionRepository.getTransaction(cacheKey, accId);
			else if (CONST_Y.equalsIgnoreCase(isOracleEnabled)) {
				response = oracleCacheRepository.getTransactionForKey(cacheKey, accId, appSource);
			}
		} catch (Exception e) {
			LOG.error("", e);
		}
		return response;
	}

	public String constructCacheKey(TransactionsRequest request, TransactionRequestParam param) {
		StringBuilder keyBuider = new StringBuilder();
		keyBuider.append(param.getRequestId()).append(param.getOrigApp())
				.append(request.getAccountInfo().getAccountId());
		return keyBuider.toString();
	}

	public boolean saveTrasactionDetail(TransactionsResponse body, String cacheKey, int sessionTimeOut,
			String appSource) {
		boolean isSaved = true;
		try {
			if (CONST_Y.equalsIgnoreCase(isRedisEnabled)) {
				isSaved = redisTransactionRepository.saveTrasactionDetail(body, cacheKey, sessionTimeOut);
			} else if (CONST_Y.equalsIgnoreCase(isOracleEnabled)) {
				oracleCacheRepository.saveTransactions(body, cacheKey, appSource);
			}
		} catch (Exception e) {
			LOG.error("", e);
			isSaved = false;
		}
		return isSaved;
	}

}
