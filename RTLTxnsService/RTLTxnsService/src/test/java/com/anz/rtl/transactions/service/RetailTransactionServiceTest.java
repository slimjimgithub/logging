package com.anz.rtl.transactions.service;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.anz.rtl.transactions.clients.BlindspotTransactionsClient;
import com.anz.rtl.transactions.clients.IntradayTransactionsClient;
import com.anz.rtl.transactions.clients.LWCEnrichTransactionsClients;
import com.anz.rtl.transactions.clients.OutstandingTransactionsClient;
import com.anz.rtl.transactions.clients.PriordayTransactionsClient;
import com.anz.rtl.transactions.dao.RedisTransactionRepository;
import com.anz.rtl.transactions.dao.RtlTxnDao;
import com.anz.rtl.transactions.request.EnrichTransactionRequest;
import com.anz.rtl.transactions.request.TransactionAccountDetailRequest;
import com.anz.rtl.transactions.request.TransactionRequestParam;
import com.anz.rtl.transactions.request.TransactionsRequest;
import com.anz.rtl.transactions.response.AdditionalMerchantDetails;
import com.anz.rtl.transactions.response.BPayDetails;
import com.anz.rtl.transactions.response.EnrichTransactionResponse;
import com.anz.rtl.transactions.response.RecordControlResponse;
import com.anz.rtl.transactions.response.Status;
import com.anz.rtl.transactions.response.TransactionsData;
import com.anz.rtl.transactions.response.TransactionsResponse;
import com.anz.rtl.transactions.util.RetailPartitionCacheUtil;
import com.anz.rtl.transactions.util.ServiceFailedException;
import com.anz.rtl.transactions.util.SessionExpiredException;
import com.anz.rtl.transactions.util.ValidationException;

//@RunWith(MockitoJUnitRunner.class)
@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource("classpath:application.properties")
public class RetailTransactionServiceTest extends CamelTestSupport {
	private static final Logger LOG = LoggerFactory.getLogger(RetailTransactionServiceTest.class);

	@Mock
	TransactionRequestParam param;
	@Mock
	TransactionsData transactionData;

	@Mock
	TransactionsResponse transactionResponse;

	@Mock
	TransactionsRequest request;

	@Mock
	RtlTxnDao rtlTxnDao;

	@Mock
	private CacheService cacheService;

	@Mock
	RetailPartitionCacheUtil retailPartitionCacheUtil;

	@InjectMocks
	RetailTransactionService rtlTxnService;
	@Mock
	private LWCEnrichTransactionsClients enrichTxnClient;

	@Mock
	TransactionsResponse res;

	@Mock
	TransactionsResponse resMock;

	@Mock
	RetailTransactionService rtlTxnServiceMock;

	@Mock
	TransactionAccountDetailRequest accountInfo;

	@Mock
	PriordayTransactionsClient priordayTxnsClient;

	@Mock
	BlindspotTransactionsClient blindSpotClient;

	@Mock
	OutstandingTransactionsClient outStdTxnCli;

	@Mock
	IntradayTransactionsClient intarTxnCli;

	@Mock
	PrepareResponse prepareResponse;

	@Mock
	RedisTransactionRepository redisRepo;

	@Mock
	EnrichTransactionService enrichService;

	@Test
	public void flowDecisionProcessorTestForPriorDay() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableRedis", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-01-01", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";

		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(Util.getCursorProdType(Mockito.anyString())).thenReturn(prodtype
		 * );
		 */
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);
		rtlTxnService.init();

	}

	@Test
	public void flowDecisionProcessorTestWhenAllTheFlagAreZero() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableOracleCache", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-01-01", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(),
		 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
		 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
		 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
		 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
		 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(createResponse());
		 * Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).
		 * thenReturn(createResponse());
		 */
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
			rtlTxnService.init();
		} catch (final ValidationException e) {
			final String msg = " Atleast one the Txn Flag should be sent ";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestWhenPriorDayAndBlindSpot() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-05-17", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);
	}

	@Test
	public void flowDecisionProcessorTestWhenOutStandingAndBlindSpot() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-05-17", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);
	}

	@Test
	public void flowDecisionProcessorTestWhenPriorDayFails() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-05-17", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from priorday service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestWhenBlindSpotFails() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		Date startDate = stringToDate("2019-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2020-01-01", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from blindspot service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForOutStanding() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-05-17", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);
	}

	@Test
	public void flowDecisionProcessorTestForOutStandingFailed() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-05-17", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	public Date stringToDate(String date, String formats) {
		DateFormat format = new SimpleDateFormat(formats, Locale.ENGLISH);
		Date ddate = null;
		try {
			ddate = format.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		return ddate;
	}

	@Test
	public void flowDecisionProcessorTestWhenPriorDayWithCursorOutstandingAndDateFlagIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		String cursor = "RED:df49cd3e-11bc-4e85-a63c-b1e680e1xEk9OOO4072200015623730:2";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-02-16", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(param.getCursor()).thenReturn(cursor);
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(null);
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(null);
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(null);
		Mockito.when(cacheService.getTransaction(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(getTransactionMap());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);
	}

	private Map<String, TransactionsResponse> getTransactionMap() {
		Map<String, TransactionsResponse> maph = new HashMap<>();
		maph.put("12345", createResWithTxn1());
		return maph;
	}

	private TransactionsResponse createResWithTxn1() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		response.setTransactions(List());
		return response;
	}

	private List<TransactionsData> List() {

		List<TransactionsData> transactions = new ArrayList<>();
		TransactionsData transactionsData = new TransactionsData();
		transactionsData.setPostedDate(stringToDate("2019-02-06", "yyyy-MM-dd"));
		transactionsData.setStatus("POSTED");
		transactionsData.setTransactionType("12");
		transactionsData.setTransactionCode("05273");
		transactionsData.setDesc1("CASH CHEQUE");
		transactionsData.setAmount(new BigDecimal(-105.6));
		transactionsData.setRunningBalance(new BigDecimal(620118));
		transactionsData.setAuxDom("000000");
		transactionsData.setExAuxDom("0000000000");
		transactionsData.setChannelCode("00405");
		TransactionsData transactionsData1 = new TransactionsData();
		transactionsData1.setPostedDate(stringToDate("2019-02-06", "yyyy-MM-dd"));
		transactionsData1.setStatus("POSTED");
		transactionsData1.setTransactionType("12");
		transactionsData1.setTransactionCode("05273");
		transactionsData1.setDesc1("CASH CHEQUE");
		transactionsData1.setAmount(new BigDecimal(-105.6));
		transactionsData1.setRunningBalance(new BigDecimal(620118));
		transactionsData1.setAuxDom("000000");
		transactionsData1.setExAuxDom("0000000000");
		transactionsData1.setChannelCode("00405");
		TransactionsData transactionsData2 = new TransactionsData();
		transactionsData2.setPostedDate(stringToDate("2019-02-06", "yyyy-MM-dd"));
		transactionsData2.setStatus("POSTED");
		transactionsData2.setTransactionType("12");
		transactionsData2.setTransactionCode("05273");
		transactionsData2.setDesc1("CASH CHEQUE");
		transactionsData2.setAmount(new BigDecimal(-105.6));
		transactionsData2.setRunningBalance(new BigDecimal(620118));
		transactionsData2.setAuxDom("000000");
		transactionsData2.setExAuxDom("0000000000");
		transactionsData2.setChannelCode("00405");
		transactions.add(transactionsData);
		transactions.add(transactionsData1);
		transactions.add(transactionsData2);
		return transactions;
	
	}

	@Test
	public void flowDecisionProcessorTestForPriorDayAndOutStandingWhenDateChkIsTrueAndBlindSpotIsAlsoTrue() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2020-02-16", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestWhenPriorDayWithOutstandingAndIntradayAndDateFlagIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-03-30", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);
	}

	@Test
	public void flowDecisionProcessorTestWhenPriorDayAndDateFlagIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-03-30", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);
	}

	@Test
	public void flowDecisionProcessorTestWhenOutstandingAndDateFlagIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-03-30", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);
	}

	@Test
	public void flowDecisionProcessorTestWhenPriorDayWithIntraDayAndDateFlagIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-03-30", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);
	}

	@Test
	public void flowDecisionProcessorTestWhenIntraDayAndDateFlagIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-03-30", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);
	}

	@Test
	public void flowDecisionProcessorTestForOutStandingWhenDateChkIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-03-30", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForPriorDayAndOutStandingWhenDateChkIsTrueAndBlindSpotIsTrue() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDay() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		/*
		 * try { Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(
		 * dateMapBlindSpot()); } catch (SQLException e) { // TODO Auto-generated catch
		 * block if (LOG.isInfoEnabled()) { LOG.info(e.getMessage()); } }
		 */
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (Exception e) {
			//final String msg = "Failed to get response from outstanding service";
			//assertEquals(msg, e.getMessage());
		}
	
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayWithDateChkFlagFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-03-30", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		/*
		 * Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(),
		 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
		 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
		 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
		 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
		 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(createResponse());
		 */
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndOutStandingWithDateChkFlagTrue() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndOutStandingWithDateChkFlagFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (Exception e) {
			final String msg = "Failed to get response from outstanding service";
			//assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndPriorDayWithDateChkFlagAndBlindSpotFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponseWithTxn());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (Exception e) {
			//final String msg = "Failed to get response from intraday service";
			//assertEquals(msg, e.getMessage());
		}

	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndPriorDayWithDateChkFlagAndBlindSpotTrue() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndPriorDayWithDateChkFlagIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-02-30", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndPriorDayAndOutStandingWithDateChkFlagAndBlindSpotTrue() {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(blindSpotClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndPriorDayAndOutStandingWithDateChkFlagIsTrueAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			// assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForPriorDayAndOutStandingWithDateChkFlagIsTrueAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			// assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndPriorDayWithDateChkFlagIsTrueAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			// assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndOutStandingWithDateChkFlagIsTrueAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "isCacheEnabled", true);
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayWithDateChkFlagIsTrueAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForPriorDayWithDateChkFlagIsTrueAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			// assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForOutStandingWithDateChkFlagIsTrue() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndPriorDayAndOutStandingWithDateChkFlagIsFalseAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-02-30", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForOutStandingWithTxn() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "CDA";
		int LowChequeNum = 1;
		int HighChequeNum = 10;
		ResponseEntity<TransactionsResponse> response = null;
		int recordLimit = 0;

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getLowChequeNum()).thenReturn(LowChequeNum);
		Mockito.when(param.getHighChequeNum()).thenReturn(HighChequeNum);
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getRecordLimit()).thenReturn(recordLimit);

		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponseWithTxn());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());
		Mockito.when(redisRepo.constructRedisKey(Mockito.any(), Mockito.any())).thenReturn("Red123");
		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ValidationException e) {
			final String msg = " Atleast one the Txn Flag should be sent ";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayWithDateChkFlagIsFalseAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-02-30", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForPriorDayWithDateChkFlagIsFalseAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-02-30", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForOutStandingWithDateChkFlagIsFalseAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-02-30", "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndOutStandingWithDateChkFlagIsFalseAndblindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-02-30", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");

		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	public RecordControlResponse recordControlResponse() {
		RecordControlResponse recordControlResponse = new RecordControlResponse();
		recordControlResponse.setRecordSent(1);
		recordControlResponse.setCursor("RED:df49cd3e-11bc-4e85-a63c-b1e680e1xEk9:10");
		return recordControlResponse;
	}

	public Map<String, Object> dateMap() {
		Map<String, Object> dateMap = new HashMap<String, Object>();
		dateMap.put("FM_NXT_SCH_DT-1", stringToDate("2019-03-04", "yyyy-MM-dd"));
		dateMap.put("FM_NXT_SCH_DT", stringToDate("2019-03-05", "yyyy-MM-dd"));
		return dateMap;
	}

	public Map<String, Object> dateMapBlindSpot() {
		Map<String, Object> dateMap = new HashMap<String, Object>();
		dateMap.put("FM_NXT_SCH_DT-1", stringToDate("2020-05-18", "yyyy-MM-dd"));
		dateMap.put("FM_NXT_SCH_DT", stringToDate("2019-05-19", "yyyy-MM-dd"));
		return dateMap;
	}

	public ResponseEntity<TransactionsResponse> createErrorResponse() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Failed", 400));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);
		return responseEntity;
	}

	public ResponseEntity<TransactionsResponse> createResponse() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		return responseEntity;
	}

	public ResponseEntity<TransactionsResponse> createResponseWithTxn() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		response.setTransactions(txnList());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);
		return responseEntity;
	}

	public ResponseEntity<TransactionsResponse> createResWithTxn() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(recordControlResponse());
		response.setTransactions(txnList());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);
		return responseEntity;
	}

	public ResponseEntity<TransactionsResponse> createRespWithTxn() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResp());
		response.setTransactions(txnList());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);
		return responseEntity;
	}

	public TransactionsResponse createResponseWithTxn1() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		response.setTransactions(txnList());
		return response;
	}

	public List<TransactionsData> txnList() {
		List<TransactionsData> transactions = new ArrayList<>();
		TransactionsData transactionsData = new TransactionsData();
		transactionsData.setPostedDate(stringToDate("2019-02-06", "yyyy-MM-dd"));
		transactionsData.setStatus("POSTED");
		transactionsData.setTransactionType("12");
		transactionsData.setTransactionCode("05273");
		transactionsData.setDesc1("CASH CHEQUE");
		transactionsData.setAmount(new BigDecimal(-105.6));
		transactionsData.setRunningBalance(new BigDecimal(620118));
		transactionsData.setAuxDom("000000");
		transactionsData.setExAuxDom("0000000000");
		transactionsData.setChannelCode("00405");
		TransactionsData transactionsData1 = new TransactionsData();
		transactionsData1.setPostedDate(stringToDate("2019-02-06", "yyyy-MM-dd"));
		transactionsData1.setStatus("POSTED");
		transactionsData1.setTransactionType("12");
		transactionsData1.setTransactionCode("05273");
		transactionsData1.setDesc1("CASH CHEQUE");
		transactionsData1.setAmount(new BigDecimal(-105.6));
		transactionsData1.setRunningBalance(new BigDecimal(620118));
		transactionsData1.setAuxDom("000000");
		transactionsData1.setExAuxDom("0000000000");
		transactionsData1.setChannelCode("00405");
		transactions.add(transactionsData);
		transactions.add(transactionsData1);
		return transactions;
	}

	public Map<String, TransactionsResponse> getTransactionMapForRedis() {
		Map<String, TransactionsResponse> maph = new HashMap<>();
		maph.put("12345", createResponseWithTxn1());
		return maph;
	}

	public TransactionAccountDetailRequest setAccInfo() {
		TransactionAccountDetailRequest accInfo = new TransactionAccountDetailRequest();
		accInfo.setAccountId("12345");
		accInfo.setCountryCode("AU");
		accInfo.setCurrencyCode("AUD");
		accInfo.setProductType("PCB");
		return accInfo;
	}

	public RecordControlResponse setRecordResponse() {
		RecordControlResponse recResponse = new RecordControlResponse();
		recResponse.setRecordSent(0);
		return recResponse;
	}

	public RecordControlResponse setRecordResp() {
		RecordControlResponse recResponse = new RecordControlResponse();
		recResponse.setRecordSent(1);
		recResponse.setCursor("RED:df49cd3e-11bc-4e85-a63c-b1e680e1xEk9OOO4072200015623730:2");
		return recResponse;
	}

	public CompletableFuture<ResponseEntity<TransactionsResponse>> makeCompletableFuture(
			ResponseEntity<TransactionsResponse> response) {
		return CompletableFuture.completedFuture(response);
	}

	public ResponseEntity<TransactionsResponse> callPriorDay() {
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyBoolean(),
				Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(),
				Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(createResponse());
		return null;
	}

	@Test
	public void flowDecisionProcessorTestUsingCursor() { 
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		String cursor = "RED:df49cd3e-11bc-4e85-a63c-b1e680e1xEk9OOO4072200015623730:2";
		String origapp = "THA";
		String acctType = "PCB";
		int priorDayTxnFlag = 1;
		int recordlimit = 10;
		Mockito.when(param.getRecordLimit()).thenReturn(recordlimit);
		Mockito.when(param.getCursor()).thenReturn(cursor);
		Mockito.when(param.getOrigApp()).thenReturn(origapp);
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		// ResponseEntity<TransactionsResponse> response = null;
		TransactionsResponse res = new TransactionsResponse();
		RecordControlResponse contr = new RecordControlResponse();
		contr.setCursor("RED");
		res.setControlRecord(contr);

		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createRespWithTxn());

		Mockito.when(cacheService.getTransaction(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(getTransactionMapForRedis());
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(res, HttpStatus.OK);
		rtlTxnService.flowDecisionProcessor(param, request, resp);

	}

	/*
	 * @Test public void
	 * flowDecisionProcessorTestWhenIntraDayWithOutstandingAndDateFlagIsFalse() {
	 * ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
	 * ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
	 * ResponseEntity<TransactionsResponse> response = null; Date startDate =
	 * stringToDate("2018-11-01", "yyyy-MM-dd"); Date endDate =
	 * stringToDate("2019-03-30", "yyyy-MM-dd"); int intradayTxnFlag = 1; int
	 * outstandingTxnFlag = 1; int priorDayTxnFlag = 0; String acctType = "PCB";
	 * String origapp = "THA"; String cursor =
	 * "RED:df49cd3e-11bc-4e85-a63c-b1e680e1xEk9OOO4072200015623730:2";
	 * Mockito.when(param.getCursor()).thenReturn(cursor);
	 * Mockito.when(param.getOrigApp()).thenReturn(origapp); Integer recordlimit=-1;
	 * Mockito.when(param.getRecordLimit()).thenReturn(recordlimit);
	 * Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
	 * Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
	 * Mockito.when(param.getStartDate()).thenReturn(startDate);
	 * Mockito.when(param.getEndDate()).thenReturn(endDate);
	 * Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
	 * Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
	 * Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
	 * Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
	 * Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).
	 * thenReturn(createResWithTxn());
	 * 
	 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
	 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
	 * 
	 * Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
	 * Mockito.any())).thenReturn(createResWithTxn());
	 * Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
	 * Mockito.any())).thenReturn(createResWithTxn());
	 * 
	 * Mockito.when(cacheService.getTransaction(Mockito.any(), Mockito.any(),
	 * Mockito.any())) .thenReturn(getTransactionMapForRedis());
	 * 
	 * try { Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(
	 * dateMap()); } catch (SQLException e) { // TODO Auto-generated catch block if
	 * (LOG.isInfoEnabled()) { LOG.info(e.getMessage()); } } Mockito.when(
	 * retailPartitionCacheUtil.getParttionKey(Mockito.anyString(),
	 * Mockito.anyString(), Mockito.anyString())) .thenReturn(104);
	 * rtlTxnService.flowDecisionProcessor(param, request, response); }
	 */

	/*
	 * @Test public void flowDecisionProcessorTestUsingCISCursor() {
	 * ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
	 * String cursor =
	 * "RED:df49cd3e-11bc-4e85-a63c-b1e680e1xEk9OOO4072200015623730:2"; String
	 * origapp = "THA"; String acctType = "PCB"; int priorDayTxnFlag = 1;
	 * Mockito.when(param.getCursor()).thenReturn(cursor);
	 * Mockito.when(param.getOrigApp()).thenReturn(origapp);
	 * Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
	 * Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
	 * Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
	 * Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
	 * ResponseEntity<TransactionsResponse> response = null;
	 * retailPartitionCacheUtil.getParttionKey(Mockito.any(),Mockito.any(),Mockito.
	 * any()); rtlTxnService.flowDecisionProcessor(param, request, response);
	 * 
	 * }
	 * 
	 */
	@Test
	public void flowDecisionProcessorTestforPriorDayAndIntraDay() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-01-01", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "FM";

		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());

		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		/*
		 * Mockito.when(Util.getCursorProdType(Mockito.anyString())).thenReturn(prodtype
		 * );
		 */
		/*
		 * try { Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(
		 * dateMap()); } catch (SQLException e) { // TODO Auto-generated catch block if
		 * (LOG.isInfoEnabled()) { LOG.info(e.getMessage()); } }
		 */
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		rtlTxnService.flowDecisionProcessor(param, request, response);

	}

	@Test
	public void testRtlEnrichTxnsService() {
		EnrichTransactionRequest req = null;
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-01-01", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		when(enrichTxnClient.enrichTransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(createErrResponse());
		rtlTxnService.rtlEnrichTxnsService(request, param, req);
	}

	private ResponseEntity<EnrichTransactionResponse> createErrResponse() {
		EnrichTransactionResponse response = new EnrichTransactionResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		// response.setResponseDate(date);
		response.setStatus(new Status("Failed", 400));
		// response.setAccountInfo(setAccInfo());
		// response.setControlRecord(setRecordResponse());
		ResponseEntity<EnrichTransactionResponse> responseEntity = new ResponseEntity<EnrichTransactionResponse>(
				response, HttpStatus.OK);
		return responseEntity;
	}

	private ResponseEntity<EnrichTransactionResponse> response() {
		// TODO Auto-generated method stub
		EnrichTransactionResponse resp = new EnrichTransactionResponse();
		resp.setRequestId("1234567");
		List<AdditionalMerchantDetails> merchantData = new ArrayList<AdditionalMerchantDetails>();
		AdditionalMerchantDetails det = new AdditionalMerchantDetails();
		det.setCal("PLUMTONAU");
		merchantData.add(det);
		resp.setMerchantData(merchantData);
		ResponseEntity<EnrichTransactionResponse> res = new ResponseEntity<EnrichTransactionResponse>(resp,
				HttpStatus.ACCEPTED);
		return res;
	}

	@Test
	public void testRtlEnrichTxnsServiceForExceptOKResponse() {
		EnrichTransactionRequest req = null;
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-01-01", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		when(enrichTxnClient.enrichTransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(res());
		rtlTxnService.rtlEnrichTxnsService(request, param, req);
	}

	private ResponseEntity<EnrichTransactionResponse> res() {
		// TODO Auto-generated method stub
		ResponseEntity<EnrichTransactionResponse> res = new ResponseEntity<EnrichTransactionResponse>(
				HttpStatus.ACCEPTED);
		return res;
	}

	@Test
	public void testGetEligibleCals() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enablePayloadLog", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "PCB";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		String Transactioncode = "512";
		String merchantName = "PAYPALAU";
		String cardUsed = "4557";
		int priorDayTxnFlag = 1;
		String cursor = "CIS";
		Mockito.when(transactionData.getTransactionCode()).thenReturn(Transactioncode);
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(transactionData.getMerchantName()).thenReturn(merchantName);
		Mockito.when(transactionData.getCardUsed()).thenReturn(cardUsed);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getCursor()).thenReturn(cursor);
		TransactionsResponse respon = new TransactionsResponse();
		respon.setRequestId("47853b3a-32bf-fd0f-d146-0f051af77ff9");
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<>(respon, HttpStatus.PARTIAL_CONTENT);
		try {
			rtlTxnService.getTrasaction(param, request, resp);
		} catch (Exception e) {

		}
	}

	@Test
	public void testGetEligibleCalsForOutStanding() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableOracleCache", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableRedis", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "PCB";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date valueDate = stringToDate("2018-11-19", "yyyy-MM-dd");
		int outstandingTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		// List<TransactionsResponse> res = new ArrayList<TransactionsResponse>();
		TransactionsResponse respon = new TransactionsResponse();
		// respon.setRequestId("47853b3a-32bf-fd0f-d146-0f051af77ff9");
		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setTransactionCode("512");
		data.setValueDate(valueDate);
		// data.setForeignExchangeRt(12.34563567845673456789);
		data.setPayeeAccNbr("213445");
		data.setPayerBSB("");
		data.setEnrichmentID("");
		data.setMerchantCountry("");
		data.setSeqNo(1);
		data.setReason("");
		data.setClearingSubMethod("");
		data.setClearingMethod("");
		data.setClearingSubPref("");
		data.setClearingPref("");
		data.setCustomerRef("");
		data.setAliasId("");
		data.setPayee("");
		data.setPayer("");
		data.setExtendedNarrative("");
		data.setPuId("");
		data.setPlanSeqNum(1);
		data.setPlanNum(1);
		data.setMerchantCategoryCode("");
		data.setConversionFee("");
		data.setOriginalCurrencyAmount("");
		data.setCardSequence(1);
		data.setMerchantSettlementId("");
		data.setReferenceNum("");
		data.setChequeSerialNum(1);
		data.setExAuxDom("");
		data.setAuthorisationSource("");
		data.setPayerAccNbr("");
		data.setPayeeBSB("");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		respon.setTransactions(transdata);
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createRes());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createRes());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(respon, HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createRes() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setTransactionCode("512");
		data.setSortingTransStatus(1);
		data.setMerchantName("AUAUS");
		data.setTransactionCode("512");
		data.setCardUsed("54557");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForIntraDay() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "PCB";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResp());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResp());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createResp() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(2);
		// data.setMerchantName("AUAUS");
		data.setTransactionCode("02");
		data.setCardUsed("54557");
		data.setTransactionType("02");
		data.setDesc2("BILLED FINANCE CHARGES");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForIntraDayDesc2() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "PCB";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createRespForIntra());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createRespForIntra());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createRespForIntra() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(2);
		// data.setMerchantName("AUAUS");
		data.setTransactionCode("02");
		data.setCardUsed("54557");
		data.setTransactionType("20");
		data.setDesc2("BILLED FINANCE CHARGES");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForPriorDay() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "PCB";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int priorDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createRespForPriorDay());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createRespForPriorDay());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createRespForPriorDay() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(3);
		// data.setMerchantName("AUAUS");
		data.setTransactionCode("21");
		data.setCardUsed("54557");
		data.setTransactionType("20");
		data.setDesc1("BILLED FINANCE CHARGES ");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForDebitCardOutStanding() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "CDA";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int priorDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createRespForDebitCardOutStanding());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any()))
				.thenReturn(createRespForDebitCardOutStanding());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createRespForDebitCardOutStanding() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(1);
		data.setMerchantName("AUAU");
		data.setTransactionCode("21");
		data.setCardUsed("4557");
		data.setTransactionType("20");
		data.setDesc1("BILLED FINANCE CHARGES ");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForDebitCardIntraDay() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "CDA";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int intraDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getIntradayFlag()).thenReturn(intraDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createRespForDebitCardIntraDay());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any()))
				.thenReturn(createRespForDebitCardIntraDay());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createRespForDebitCardIntraDay() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(2);
		data.setMerchantName("AUAU");
		data.setTransactionCode("02");
		data.setCardUsed("4557");
		data.setTransactionType("02");
		data.setDesc2("BILLED FINANCE CHARGES ");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForDebitCardIntra() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "CDA";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int intraDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getIntradayFlag()).thenReturn(intraDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createRespForDebitCardIntra());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any()))
				.thenReturn(createRespForDebitCardIntra());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createRespForDebitCardIntra() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(2);
		data.setMerchantName("AUAU");
		data.setTransactionCode("02");
		data.setCardUsed("4557");
		data.setTransactionType("19");
		data.setDesc2("BILLED FINANCE CHARGES ");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForDebitCardPriorDay() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "CDA";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int priorDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createRespForDebitCardPriorDay());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any()))
				.thenReturn(createRespForDebitCardPriorDay());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createRespForDebitCardPriorDay() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResp());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(3);
		data.setMerchantName("AUAU");
		data.setTransactionCode("01105");
		data.setCardUsed("4557");
		data.setTransactionType("19");
		data.setDesc2("BILLED FINANCE CHARGES ");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForDebitCardPRiorDay() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "CDA";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int priorDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createRespForDebitCardPriorDAY());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any()))
				.thenReturn(createRespForDebitCardPriorDAY());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createRespForDebitCardPriorDAY() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(3);
		data.setMerchantName("AUAU");
		data.setTransactionCode("01041");
		data.setCardUsed("4557");
		data.setTransactionType("19");
		data.setChannelCode("00111");
		data.setDesc1("BILLED FINANCE CHARGES ");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForDebitCardPrior() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "CDA";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int priorDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponseForDebitCardPriorDay());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any()))
				.thenReturn(createResponseForDebitCardPriorDay());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createResponseForDebitCardPriorDay() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(3);
		data.setMerchantName("AUAU");
		data.setTransactionCode("05267");
		data.setCardUsed("4557");
		data.setTransactionType("19");
		data.setDesc1("BILLED FINANCE CHARGES ");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForDebitCardPriorDAY() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "CDA";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int priorDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(param.getRecordLimit()).thenReturn(10);
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponseForDebitPriorDay());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any()))
				.thenReturn(createResponseForDebitPriorDay());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createResponseForDebitPriorDay() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(3);
		data.setMerchantName("AUAU");
		data.setTransactionCode("05437");
		data.setCardUsed("4557");
		data.setTransactionType("19");
		data.setDesc1("BILLED FINANCE CHARGES ");
		data.setDesc2("BILLED FINANCE CHARGES ");
		data.setDesc3("BILLED FINANCE CHARGES ");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void testGetEligibleCalsForDebitCardoutStanding() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "CDA";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int priorDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponForDebitCardOutStanding());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any()))
				.thenReturn(createResponForDebitCardOutStanding());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	public ResponseEntity<TransactionsResponse> createResponForDebitCardOutStanding() {
		TransactionsResponse response = new TransactionsResponse();
		response.setRequestId("54321");
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		response.setResponseDate(date);
		response.setStatus(new Status("Success", 200));
		response.setAccountInfo(setAccInfo());
		response.setControlRecord(setRecordResponse());
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);

		TransactionsData data = new TransactionsData();
		data.setTransactionId("14568");
		data.setSortingTransStatus(1);
		data.setMerchantName("PAYPALAUS");
		data.setTransactionCode("21");
		data.setCardUsed("5457");
		data.setTransactionType("20");
		data.setDesc1("BILLED FINANCE CHARGES ");
		List<TransactionsData> transdata = new ArrayList<TransactionsData>();
		transdata.add(data);
		response.setTransactions(transdata);
		return responseEntity;
	}

	@Test
	public void flowDecisionProcessorTestSessionTimeOut() {
		String cursor = "RED:df49cd3e-11bc-4e85-a63c-b1e680e1xEk9OOO4072200015623730:2";
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-02-30", "yyyy-MM-dd");
		// int intradayTxnFlag = 1;
		// int outstandingTxnFlag = 0;
		// int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(param.getCursor()).thenReturn(cursor);
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		try {
			Mockito.when(cacheService.getTransaction(Mockito.any(), Mockito.any(), Mockito.any()))
					.thenReturn(new HashMap());

			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final SessionExpiredException e) {
			final String msg = "Request TimeOut";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void testSetResponse() {
		String redisKey = "UID";
		int availabletrans = 1;
		int startIndex = 1;
		int recordlimit = 0;
		when(param.getRecordLimit()).thenReturn(recordlimit);
		List<TransactionsData> data = new ArrayList<TransactionsData>();
		TransactionsData transdata = new TransactionsData();
		transdata.setTransactionCode("512");
		BPayDetails bpayDetails = new BPayDetails();
		bpayDetails.setApcaNum("123");
		transdata.setBpayDetails(bpayDetails);
		data.add(transdata);
		TransactionAccountDetailRequest transreq = new TransactionAccountDetailRequest();
		transreq.setAccountId("0004567896435789");
		request.setAccountInfo(accountInfo);
		rtlTxnService.setResponse(data, request, param, redisKey, availabletrans, startIndex);

	}

	@Test
	public void testGetEligibleCalsForPriorDayandOutstanding() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "PCB";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResp());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResp());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResp());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	@Test
	public void testGetEligibleCalsForIntradayAndPriorDay() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "PCB";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int priorDayTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResp());
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResp());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResp());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	@Test
	public void testGetEligibleCalsForIntradayAndPriorDayAndOutstanding() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		String acctType = "PCB";
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int priorDayTxnFlag = 1;
		int outstandingTxnFlag = 1;
		// String MerchantInfo="1";
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getIncludeMerchantInfo()).thenReturn(1);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getStartDate()).thenReturn(startDate);

		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResp());
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResp());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResp());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResp());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		ResponseEntity<TransactionsResponse> resp = new ResponseEntity<TransactionsResponse>(HttpStatus.OK);
		rtlTxnService.getTrasaction(param, request, resp);

	}

	@Test
	public void flowDecisionProcessorTestWhenIntraDayFails() throws SQLException {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final Exception e) {

		}
	}

	@Test
	public void flowDecisionProcessorTestWhenOutstandingFails() throws SQLException {
		ReflectionTestUtils.setField(rtlTxnService, "enablePartialSuccess", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ResponseEntity<TransactionsResponse> response = null;
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";

		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponse());
		Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMap());

		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final Exception e) {

		}
	}

	@Test
	public void flowDecisionProcessorTestForIntraDayAndOutStandingWithDateChkFlagIsTrueAndBlindSpotIsFalse() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		ReflectionTestUtils.setField(rtlTxnService, "isCacheEnabled", true);
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 0;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		Mockito.when(param.getSessionTimeOut()).thenReturn(10);
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());
		Mockito.when(cacheService.saveTrasactionDetail(Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.any()))
				.thenReturn(true);
		Mockito.when(cacheService.constructCacheKey(Mockito.any(), Mockito.any())).thenReturn("Gateway");

		try {
			Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(dateMapBlindSpot());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (final ServiceFailedException e) {
			final String msg = "Failed to get response from outstanding service";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void flowDecisionProcessorTestForOSPDError() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 0;
		int outstandingTxnFlag = 1;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		/*
		 * try { Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(
		 * dateMapBlindSpot()); } catch (SQLException e) { // TODO Auto-generated catch
		 * block if (LOG.isInfoEnabled()) { LOG.info(e.getMessage()); } }
		 */
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (Exception e) {
			//final String msg = "Failed to get response from outstanding service";
			//assertEquals(msg, e.getMessage());
		}
	
	}
	
	@Test
	public void flowDecisionProcessorTestForIDPDError() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		/*
		 * try { Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(
		 * dateMapBlindSpot()); } catch (SQLException e) { // TODO Auto-generated catch
		 * block if (LOG.isInfoEnabled()) { LOG.info(e.getMessage()); } }
		 */
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (Exception e) {
			//final String msg = "Failed to get response from outstanding service";
			//assertEquals(msg, e.getMessage());
		}
	
	}
	
	@Test
	public void flowDecisionProcessorTestForIDOSPDError() {
		ReflectionTestUtils.setField(rtlTxnService, "enableDateValidation", "Y");
		Date startDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");
		int intradayTxnFlag = 1;
		int outstandingTxnFlag = 0;
		int priorDayTxnFlag = 1;
		String acctType = "PCB";
		ResponseEntity<TransactionsResponse> response = null;
		accountInfo.setAccountId("12345");
		Mockito.when(request.getAccountInfo()).thenReturn(accountInfo);
		Mockito.when(request.getAccountInfo().getAccountId()).thenReturn("12345");
		Mockito.when(param.getStartDate()).thenReturn(startDate);
		Mockito.when(param.getEndDate()).thenReturn(endDate);
		Mockito.when(param.getIntradayFlag()).thenReturn(intradayTxnFlag);
		Mockito.when(param.getOutstandingFlag()).thenReturn(outstandingTxnFlag);
		Mockito.when(param.getPriordayFlag()).thenReturn(priorDayTxnFlag);
		Mockito.when(request.getAccountInfo().getProductType()).thenReturn(acctType);
		/*
		 * Mockito.when(rtlTxnServiceMock.callOutstanding(Mockito.any(), Mockito.any(),
		 * Mockito.any())).thenReturn(makeCompletableFuture(createResponse()));
		 */
		Mockito.when(priordayTxnsClient.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(intarTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(outStdTxnCli.rtltransactions(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(createErrorResponse());
		Mockito.when(prepareResponse.prepareTransactionResponse(Mockito.any())).thenReturn(createResponseWithTxn());

		/*
		 * try { Mockito.when(rtlTxnDao.selectBlackOut(Mockito.anyString())).thenReturn(
		 * dateMapBlindSpot()); } catch (SQLException e) { // TODO Auto-generated catch
		 * block if (LOG.isInfoEnabled()) { LOG.info(e.getMessage()); } }
		 */
		Mockito.when(
				retailPartitionCacheUtil.getParttionKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(104);
		try {
			rtlTxnService.flowDecisionProcessor(param, request, response);
		} catch (Exception e) {
			//final String msg = "Failed to get response from outstanding service";
			//assertEquals(msg, e.getMessage());
		}
	
	}

}
