package com.anz.rtl.transactions.dao;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.anz.rtl.transactions.constants.TransactionConstants;
import com.anz.rtl.transactions.request.TransactionRequestParam;
import com.anz.rtl.transactions.request.TransactionsRequest;
import com.anz.rtl.transactions.response.TransactionsResponse;

@Component
public class RedisTransactionRepository {
    @Autowired
    private RedisTemplate redisTemplate;
    private static final Logger LOG = LoggerFactory.getLogger(RedisTransactionRepository.class);

    private HashOperations<String, String, TransactionsResponse> hashOperation;

    public boolean saveTrasactionDetail(TransactionsResponse response, String keyBuider, int timeout) {
    	boolean radisEnable=true;
        try {
        	Long redisInsertStart = System.currentTimeMillis();
            hashOperation = redisTemplate.opsForHash();
            hashOperation.put(keyBuider, response.getAccountInfo().getAccountId(), response);
            //CISP-1771- fix-must use the Default session-Timeout as 30 Minutes if the session-Timeout sent in the request is greater than 30 Minutes.
            if(timeout>30) {
            	timeout=30;
            }
            redisTemplate.expire(keyBuider, timeout, TimeUnit.MINUTES);
            MDC.put(TransactionConstants.REDIS_INSERT, String.valueOf(System.currentTimeMillis() - redisInsertStart));
            MDC.put(TransactionConstants.REDIS_RECORD_COUNT, String.valueOf(response.getTransactions().size()));
           
        } catch (RedisConnectionFailureException e) {
            LOG.info("Redis connection Failure Exception saveTrasactionDetail(): ", e.getCause());
            radisEnable=false;
        }
        return radisEnable;
    }
    
    /*public void deleteKey(Object key) {
    	Boolean flag = redisTemplate.delete(key);
    	System.out.println("Delete key" +key+ "status" +flag);
    }*/

    /*
     * public Map<String,List<TransactionsData>> getTransaction(String keyBuider) {
     * hashOperation = redisTemplate.opsForHash(); Map<String,
     * List<TransactionsData>> maph=hashOperation.entries(keyBuider); return
     * hashOperation.entries(keyBuider); }
     */

    public Map<String, TransactionsResponse> getTransaction(String keyBuider, String accId) {
        Map<String, TransactionsResponse> maph = null;
        try {
        	Long redisRetrieveStart = System.currentTimeMillis();
            hashOperation = redisTemplate.opsForHash();
            maph = hashOperation.entries(keyBuider);
            MDC.put(TransactionConstants.REDIS_RETRIEVAL, String.valueOf(System.currentTimeMillis() - redisRetrieveStart));
        } catch (RedisConnectionFailureException e) {
            LOG.info("Redis connection Failure Exception in getTransaction() : ", e.getCause());

        }
        return maph;
        // return hashOperation.get(keyBuider,accId);
    }

    public String constructRedisKey(TransactionsRequest request, TransactionRequestParam param) {
        StringBuilder keyBuider = new StringBuilder();
        keyBuider.append(param.getRequestId()).append(param.getOrigApp())
                .append(request.getAccountInfo().getAccountId());
        return keyBuider.toString();
    }
}
