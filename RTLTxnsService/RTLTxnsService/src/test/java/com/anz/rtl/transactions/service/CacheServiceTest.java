package com.anz.rtl.transactions.service;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.anz.rtl.transactions.dao.OracleCacheRepository;
import com.anz.rtl.transactions.dao.RedisTransactionRepository;
import com.anz.rtl.transactions.request.TransactionAccountDetailRequest;
import com.anz.rtl.transactions.request.TransactionRequestParam;
import com.anz.rtl.transactions.request.TransactionsRequest;
import com.anz.rtl.transactions.response.TransactionsResponse;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CacheServiceTest {
	@Mock
	private RedisTransactionRepository redisTransactionRepository;
	@Mock
	TransactionAccountDetailRequest accountInfo;

	@Mock
	private OracleCacheRepository oracleCacheRepository;

	@Mock
	TransactionsRequest request;

	@Mock
	TransactionRequestParam param;

	@InjectMocks
	CacheService cacheService;

	@Test
	public void testGetTransaction() {
		String cacheKey = "UID";
		String accId = "4072200015623730";
		String appSource = "THA";
		ReflectionTestUtils.setField(cacheService, "isRedisEnabled", "Y");
		Mockito.when(redisTransactionRepository.getTransaction(Mockito.any(), Mockito.any())).thenReturn(response());
		cacheService.getTransaction(cacheKey, accId, appSource);
	}

	@Test
	public void testGetTransactionForOracle() {
		ReflectionTestUtils.setField(cacheService, "isOracleEnabled", "Y");
		String cacheKey = "UID";
		String accId = "4072200015623730";
		String appSource = "THA";
		Mockito.when(redisTransactionRepository.getTransaction(Mockito.any(), Mockito.any())).thenReturn(response());
		cacheService.getTransaction(cacheKey, accId, appSource);

	}

	private Map<String, TransactionsResponse> response() {
		// TODO Auto-generated method stub
		Map<String, TransactionsResponse> response = new HashMap<String, TransactionsResponse>();
		TransactionsResponse res = new TransactionsResponse();
		response.put("123", res);
		return response;
	}

	@Test
	public void testConstructCacheKey() {
		String requestId = "12345";
		String orgnApp = "OOO";
		String accId = "4072200015623730";

		Mockito.when(param.getRequestId()).thenReturn(requestId);
		Mockito.when(param.getOrigApp()).thenReturn(orgnApp);
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn(accId);
		String str = cacheService.constructCacheKey(request, param);
		assertEquals("12345OOO4072200015623730", str);
	}

	@Test
	public void testSaveTrasactionDetail() {
		TransactionsResponse body = new TransactionsResponse();
		body.setRequestId("12345");
		String cacheKey = "UID";
		int sessionTimeOut = 1;
		String appSource = "THA";
		ReflectionTestUtils.setField(cacheService, "isRedisEnabled", "Y");
		Mockito.when(
				redisTransactionRepository.saveTrasactionDetail(Mockito.any(), Mockito.anyString(), Mockito.anyInt()))
				.thenReturn(trueResponse());
		cacheService.saveTrasactionDetail(body, cacheKey, sessionTimeOut, appSource);

	}

	@Test
	public void testSaveTrasactionDet() {
		TransactionsResponse body = new TransactionsResponse();
		body.setRequestId("12345");
		String cacheKey = "UID";
		int sessionTimeOut = 1;
		String appSource = "THA";
		ReflectionTestUtils.setField(cacheService, "isOracleEnabled", "Y");
		Mockito.when(
				redisTransactionRepository.saveTrasactionDetail(Mockito.any(), Mockito.anyString(), Mockito.anyInt()))
				.thenReturn(trueResponse());
		cacheService.saveTrasactionDetail(body, cacheKey, sessionTimeOut, appSource);

	}

	private Boolean trueResponse() {
		// TODO Auto-generated method stub
		Boolean issaved = true;
		return issaved;
	}

	@Test
	public void testSaveTrasactionDetails() {
		TransactionsResponse body = new TransactionsResponse();
		body.setRequestId("12345");
		String cacheKey = "UID";
		int sessionTimeOut = 1;
		String appSource = "THA";
		ReflectionTestUtils.setField(cacheService, "isOracleEnabled", "Y");
		Mockito.when(
				redisTransactionRepository.saveTrasactionDetail(Mockito.any(), Mockito.anyString(), Mockito.anyInt()))
				.thenReturn(falseResponse());
		try {
			cacheService.saveTrasactionDetail(body, cacheKey, sessionTimeOut, appSource);
		} catch (Exception e) {

		}

	}

	private Boolean falseResponse() {
		// TODO Auto-generated method stub
		Boolean issaved = false;
		return issaved;
	}
	
	@Test
	public void testsavetrasactiondetaikhl() {
		TransactionsResponse body = new TransactionsResponse();
		body.setRequestId("12345");
		String cacheKey = "UID";
		int sessionTimeOut = 1;
		String appSource = "THA";
		//ReflectionTestUtils.setField(cacheService, "redis.enable:N", "Y");
		Mockito.when(
				redisTransactionRepository.saveTrasactionDetail(Mockito.any(), Mockito.anyString(), Mockito.anyInt()))
				.thenReturn(falseResponse());
		
			cacheService.saveTrasactionDetail(body, cacheKey, sessionTimeOut, appSource);
		
	}

}
