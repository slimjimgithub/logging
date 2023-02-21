package com.anz.rtl.transactions.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.anz.rtl.transactions.clients.BlindspotTransactionsClient;
import com.anz.rtl.transactions.clients.IntradayTransactionsClient;
import com.anz.rtl.transactions.clients.LWCEnrichTransactionsClients;
import com.anz.rtl.transactions.clients.OutstandingTransactionsClient;
import com.anz.rtl.transactions.clients.PriordayTransactionsClient;
import com.anz.rtl.transactions.constants.TransactionConstants;
import com.anz.rtl.transactions.dao.RtlTxnDao;
import com.anz.rtl.transactions.request.BodyRequest;
import com.anz.rtl.transactions.request.CalInfo;
import com.anz.rtl.transactions.request.EnrichTransactionRequest;
import com.anz.rtl.transactions.request.TransactionRequestParam;
import com.anz.rtl.transactions.request.TransactionsRequest;
import com.anz.rtl.transactions.response.EnrichTransactionResponse;
import com.anz.rtl.transactions.response.RecordControlResponse;
import com.anz.rtl.transactions.response.Status;
import com.anz.rtl.transactions.response.TransactionsData;
import com.anz.rtl.transactions.response.TransactionsResponse;
import com.anz.rtl.transactions.util.RequestValidation;
import com.anz.rtl.transactions.util.RetailPartitionCacheUtil;
import com.anz.rtl.transactions.util.ServiceFailedException;
import com.anz.rtl.transactions.util.SessionExpiredException;
import com.anz.rtl.transactions.util.TransactionMdcLogger;
import com.anz.rtl.transactions.util.Util;
import com.anz.rtl.transactions.util.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Component
@EnableAsync
public class RetailTransactionService {

	private static final String TIME_ZONE = "Australia/Melbourne";
	private static final String DT_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String DT_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

	private static final Logger LOG = LoggerFactory.getLogger(RetailTransactionService.class);
	// Configurable fields
	@Value(value = "${enablePartialSuccess}")
	String enablePartialSuccess;
	@Value(value = "${redis.enable:N}")
	private String enableRedis;
	@Value("${oracle.cache.enable:Y}")
	private String enableOracleCache;
	@Value("${enableDateValidation:N}")
	private String enableDateValidation;
	@Value("${enablePayloadLog:N}")
	private String enablePayloadLog;

	public static final String SRC_APP = "RtlGateway";
	// boolean isPartialSuccess=false;
	private boolean isCacheEnabled = false;
	/*
	 * @Autowired private TransactionAccountDetailRequest txnAccReq;
	 */
	@Autowired
	private RtlTxnDao rtlTxnDao;

	@Autowired
	private CacheService cacheService;

	// private ResponseEntity<TransactionsResponse> response;
	// private ResponseEntity<AdditionalMerchantInfoResponse> enrichResponse;
	@Autowired
	private PriordayTransactionsClient priordayTxnsClient;
	@Autowired
	private BlindspotTransactionsClient blindspotTxnsClient;

	@Autowired
	private OutstandingTransactionsClient outStandingTxnClient;
	@Autowired
	private LWCEnrichTransactionsClients enrichTxnClient;
	@Autowired
	private IntradayTransactionsClient intraDayTxnClient;

	// private Map<String, Object> dateMap = null;
	// private ArrayList<ResponseEntity<TransactionsResponse>> responseList;
	@Autowired
	private PrepareResponse prepareResponse;
	@Autowired
	private RetailPartitionCacheUtil retailPartitionCacheUtil;
	
	@Autowired
	private EnrichTransactionService enrichTransactionService;

	@PostConstruct
	public void init() {
		isCacheEnabled = enableRedis.equalsIgnoreCase("Y") || enableOracleCache.equalsIgnoreCase("Y") ? true : false;
		LOG.info("Cache enabled [{}] Redis [{}] Oracle [{}]", isCacheEnabled, enableRedis, enableOracleCache);
	}

	public ResponseEntity<TransactionsResponse> flowDecisionProcessor(TransactionRequestParam param,
			TransactionsRequest request, ResponseEntity<TransactionsResponse> response) {
		RequestValidation.validateRequest(request);
		RequestValidation.ValidateRequest(param, request.getAccountInfo().getProductType(), enableDateValidation);
		// Check Redis Cursor in request
		return response = getTrasaction(param, request, response);
	}

	public ResponseEntity<TransactionsResponse> getTrasaction(TransactionRequestParam param,
			TransactionsRequest request, ResponseEntity<TransactionsResponse> response) {
		String redisKey = null;
		int availabletrans = 0;
		boolean flag = false;
		if (param.getCursor() != null && param.getCursor().startsWith("RED")) {
			LOG.info("Getting response form redis");
			String reqCursor = param.getCursor();
			String[] str = reqCursor.split(":");
			redisKey = str[1];

			// List<TransactionsData> transDatas = null;
			String cursor = null;
			// Map<String, TransactionsResponse>transactions =
			// redisRepo.getTransaction(redisKey,request.getAccountInfo().getAccountId());
			Map<String, TransactionsResponse> redisResponse = cacheService.getTransaction(redisKey,
					request.getAccountInfo().getAccountId(), param.getOrigApp());
			if (!redisResponse.isEmpty()) {
				TransactionsResponse transactionResponse = redisResponse.get(request.getAccountInfo().getAccountId());
				List<TransactionsData> transactions = transactionResponse.getTransactions();
				if (!CollectionUtils.isEmpty(transactions)) {
					// transDatas=transactions.get(request.getAccountInfo().getAccountId());
					availabletrans = transactions.size();

					int senttransactions = new Integer(str[2]);
					if (param.getRecordLimit() + senttransactions >= availabletrans) {

						List<TransactionsData> redTxns = transactions.subList(senttransactions, availabletrans);
						int recorndLimit = param.getRecordLimit() - redTxns.size();
						param.setRecordLimit(recorndLimit);
						if (transactionResponse.getControlRecord().getCursor() != null
								&& param.getPriordayFlag() == 1) {
							flag = true;
							LOG.info(
									"Redis have less transaction than requested so gateway is calling to priorday service");
							cursor = transactionResponse.getControlRecord().getCursor();
							param.setCursor(cursor);
							// Redis have less transaction than requested so gateway is calling to priorday
							// service
							response = retryPriorDay(request, param);
							List<TransactionsData> pdTrans = response.getBody().getTransactions();
							if (!CollectionUtils.isEmpty(pdTrans)) {
								redTxns.addAll(pdTrans);
							}
							response.getBody().setRequestId(param.getRequestId());
						}
						if (response == null) {
							TransactionsResponse res = new TransactionsResponse();
							RecordControlResponse controlRecord = new RecordControlResponse();
							res.setControlRecord(controlRecord);
							ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of(TIME_ZONE));
							res.setResponseDate(date);
							response = new ResponseEntity<TransactionsResponse>(res, HttpStatus.OK);
						}
						response.getBody().setTransactions(redTxns);
						response.getBody().getControlRecord().setRecordSent(redTxns.size());
						if (!flag) {
							response.getBody().getControlRecord().setCursor(null);

						}
						// response.getBody().getControlRecord().setTotalCount(redTxns.size());

						response.getBody().setRequestId(param.getRequestId());
						ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of(TIME_ZONE));
						response.getBody().setResponseDate(date);
						response.getBody().setAccountInfo(request.getAccountInfo());

					} else {
						TransactionsResponse res = null;
						res = setResponse(transactions, request, param, redisKey, availabletrans, senttransactions);
						response = new ResponseEntity<TransactionsResponse>(res, HttpStatus.OK);
					}
					response.getBody().setStatus(new Status("Success", 200));

				}
			} else {
				LOG.debug("Redis Key is expired ", redisKey);
				TransactionMdcLogger.writeToMDCLog(param.getRequestId(), "Request TimeOut", 0l, param.getOrigApp(),
						request.getAccountInfo().getProductType(), redisKey, 0, param.getCursor(), null,
						param.getStartDate(), param.getEndDate());
				throw new SessionExpiredException("Request TimeOut");

				// response = callRequestedServises(param, request);

			}
		} else if (param.getCursor() != null && param.getCursor().startsWith("CIS")) {
			Long startTs = System.currentTimeMillis();
			LOG.info("Received CIS cursor in request So gateway calling to priorday service");
			response = retryPriorDay(request, param);
			LOG.debug("PriorDay service response received. Time taken by priorday service is : "
					+ (System.currentTimeMillis() - startTs));
			LOG.debug("Total transaction sent by priorday service : "
					+ response.getBody().getControlRecord().getRecordSent());
			// return response;

		} else {
			response = callRequestedServises(param, request, response);
		}

		if (response.getBody().getStatus().getStatusCode() == 206) {
			response.getBody().setStatus(new Status("PARTIAL SUCCESS", 206));
			response.getBody().setRequestId(param.getRequestId());
			ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of(TIME_ZONE));
			response.getBody().setResponseDate(date);
		}
		if (response.hasBody() && response.getBody().getTransactions() != null && param.getIncludeMerchantInfo() == 1) {
			// get Call List
			LOG.debug("Elligible to call Transaction Enrichment Service");
			// Set<String> eligibleCals = new HashSet<String>();
			Map<String, String> eligibleCals = new HashMap<>();
			enrichTransactionService.getEligibleCals(request.getAccountInfo().getProductType(),
					response.getBody().getTransactions(), eligibleCals);
			// ArrayList<String> calList = new ArrayList<>(eligibleCals);
			LOG.info("size of elligible enrichment cal list: " + eligibleCals.size());
			LOG.debug("elligible enrichment cal list: " + eligibleCals);
			if (eligibleCals.size() > 0) {
				EnrichTransactionRequest enrichRequest = new EnrichTransactionRequest();
				CalInfo cals = new CalInfo();
				List<BodyRequest> bodyRequestList = new LinkedList<>();
				eligibleCals.forEach((cal, bankTransactionType) -> {
					BodyRequest bodyRequest = new BodyRequest();
					bodyRequest.setCal(cal);
					bodyRequest.setBankAccountTransactionType(bankTransactionType);
					if (bankTransactionType != null && !Util.isBpayOrDirectDebit(bankTransactionType)) {
						bodyRequest.setBankAccountTransactionType(null);
					}

					bodyRequestList.add(bodyRequest);
				});

				cals.setCal(bodyRequestList);
				enrichRequest.setCalInfo(cals);
				ResponseEntity<EnrichTransactionResponse> enrichResponse = rtlEnrichTxnsService(request, param,
						enrichRequest);

				if (enrichResponse != null && enrichResponse.hasBody()
						&& enrichResponse.getBody().getMerchantData().size() > 0) {
					response = prepareResponse.mergeLWCEnrichResponse(enrichResponse, response, param.getApiVersion());
				} else {
					LOG.info("Enrich detail is not available for all eligible cals: ");
				}
			}
		}
		if ("Y".equals(enablePayloadLog)) {
			ObjectMapper mapper = new ObjectMapper();
			ObjectWriter writer = mapper.writer();
			try {
				LOG.info("Gateway Request parameters : {} ", param);
				LOG.info("Gateway Request Body : {}", writer.writeValueAsString(request));
				LOG.info("Gateway response : {} ", writer.writeValueAsString(response.getBody()));
			} catch (JsonProcessingException e) {
				LOG.info("json Parsing error occur: {}", e.getCause().getMessage());
			}
		}
		return response;
	}

	
	ResponseEntity<TransactionsResponse> callRequestedServises(TransactionRequestParam param,
			TransactionsRequest request, ResponseEntity<TransactionsResponse> response) {
		boolean blindSpotFlag = false;
		boolean dateCheckFlag = true;
		String decisionFlag = null;
		Date lastBTRLoadDt = null;
		Date nextBTRLoadDt = null;
		int availabletrans = 0;
		String accountId = request.getAccountInfo().getAccountId();
		Date endDate = param.getEndDate();
		int intradayTxnFlag = param.getIntradayFlag();
		int outstandingTxnFlag = param.getOutstandingFlag();
		int priorDayTxnFlag = param.getPriordayFlag();
		String acctType = request.getAccountInfo().getProductType();
		String prodType = Util.getCursorProdType(acctType);
		LOG.debug("LWC Enrichment Value" + param.getIncludeMerchantInfo());
		Map<String, Object> dateMap = null;
		try {
			dateMap = rtlTxnDao.selectBlackOut(prodType);
		} catch (SQLException e) {
			if (LOG.isInfoEnabled()) {
				LOG.info(e.getMessage());
			}
		}
		if (dateMap != null) {
			lastBTRLoadDt = (Date) dateMap.get("FM_NXT_SCH_DT-1");
			nextBTRLoadDt = (Date) dateMap.get("FM_NXT_SCH_DT");
		}

		Integer partKey = retailPartitionCacheUtil.getParttionKey(request.getAccountInfo().getProductType(), accountId,
				acctType);

		// Do not call outstanding service if cheque filter is enabled
		if (param.getLowChequeNum() != 0 && param.getHighChequeNum() != 0 && outstandingTxnFlag == 1) {
			outstandingTxnFlag = 0;
		}

		param.setNextLoadDate(nextBTRLoadDt);
		param.setPartKey(partKey);
		decisionFlag = Integer.toString(intradayTxnFlag) + Integer.toString(priorDayTxnFlag)
				+ Integer.toString(outstandingTxnFlag);
		dateCheckFlag = endDate.after(java.sql.Date.valueOf(LocalDate.now().minusDays(11))) ? true : false;
		if (null != lastBTRLoadDt && endDate.after(lastBTRLoadDt)) {
			blindSpotFlag = true;
			param.setLastLoadDate(lastBTRLoadDt);
		}
		LOG.info(
				"Part key : {}, LastLoadDate : {}, nextLoadDate : {}, DecisionFlag : {}, Start Date : {}, End Date : {}",
				partKey, lastBTRLoadDt, nextBTRLoadDt, decisionFlag, param.getStartDate(), param.getEndDate());
		switch (decisionFlag) {
		case "000":
			throw new ValidationException(" Atleast one the Txn Flag should be sent ");
		case "001":
			if (dateCheckFlag) {
				// Send OS
				response = callServices("OS", request, param, false, response);
			} else {
				// No need to call any service because we will not get any records
				response = callServices("NA", request, param, false, response);
			}
			break;
		case "010":
			response = checkAndCallPriorDay(blindSpotFlag, request, param, response);
			break;
		case "011":
			if (dateCheckFlag) {
				if (blindSpotFlag) {
					// Send OS + PD + BS if applicable
					response = callServices("OSPDBS", request, param, false, response);
				} else {
					// Send OS + PD
					response = callServices("PDOS", request, param, false, response);
				}
			} else {
				response = checkAndCallPriorDay(blindSpotFlag, request, param, response);
			}
			break;
		case "100":
			if (dateCheckFlag) {
				// call ID
				response = callServices("ID", request, param, false, response);
			} else {
				// No need to call any service because we will not get any records
				response = callServices("NA", request, param, false, response);
			}
			break;

		case "101":
			if (dateCheckFlag) {
				// Send ID + OS
				response = callServices("IDOS", request, param, false, response);
			} else {
				// No need to call any service because we will not get any records
				response = callServices("NA", request, param, false, response);
			}
			break;

		case "110":
			if (dateCheckFlag) {
				if (blindSpotFlag) {
					// Send ID + PD + BS if applicable
					response = callServices("IDPDBS", request, param, false, response);
				} else {
					// Send ID + PD
					response = callServices("IDPD", request, param, false, response);
				}
			} else {
				response = checkAndCallPriorDay(blindSpotFlag, request, param, response);
			}
			break;
		case "111":
			if (dateCheckFlag) {
				if (blindSpotFlag) {
					// Send ID + OS + PD + BS
					response = callServices("IDOSPDBS", request, param, false, response);
				} else {
					// Send ID + OS + PD
					response = callServices("IDOSPD", request, param, false, response);
				}
			} else {
				response = checkAndCallPriorDay(blindSpotFlag, request, param, response);
			}
			break;
		default:
			break;
		}
		if (response != null) {
			int txnSize = 0;

			if (!CollectionUtils.isEmpty(response.getBody().getTransactions())) {
				txnSize = response.getBody().getTransactions().size();
				LOG.info("Total Number of Transactions" + txnSize);
			}

			if (txnSize <= param.getRecordLimit()) {
				int count = param.getRecordLimit() - txnSize;
				if (count >= 1 && param.getPriordayFlag() == 1
						&& response.getBody().getControlRecord().getCursor() != null) {
					param.setRecordLimit(count);
					param.setCursor(response.getBody().getControlRecord().getCursor());
					ResponseEntity<TransactionsResponse> responseRetry = response;
					ResponseEntity<TransactionsResponse> responsePriorDay;
					responsePriorDay = retryPriorDay(request, param);
					if (null != responsePriorDay.getBody().getTransactions()
							&& null != responseRetry.getBody().getTransactions()) {
						responseRetry.getBody().getTransactions().addAll(responsePriorDay.getBody().getTransactions());
					} else if (null == responseRetry.getBody().getTransactions()) {
						responseRetry = responsePriorDay;
					}
					responseRetry.getBody().getControlRecord()
							.setRecordSent(responseRetry.getBody().getTransactions().size());
					responseRetry.getBody().getControlRecord()
							.setCursor(responsePriorDay.getBody().getControlRecord().getCursor());
					response = responseRetry;
				}
				return response;

			} else {
				TransactionsResponse res = null;
				if (isCacheEnabled) {
					String cacheKey = cacheService.constructCacheKey(request, param);
					boolean isSavedInRedis = cacheService.saveTrasactionDetail(response.getBody(), cacheKey,
							param.getSessionTimeOut(), param.getOrigApp());
					/* Get Transactions from Redis and prepareResponse */

					if (!isSavedInRedis) {
						res = setResponse(response.getBody().getTransactions(), request, param, null,
								response.getBody().getTransactions().size(), 0);
					} else {
						/*
						 * Map<String, TransactionsResponse> redisResponse =
						 * cacheService.getTransaction(cacheKey,
						 * request.getAccountInfo().getAccountId(), param.getOrigApp()); if
						 * (!CollectionUtils.isEmpty(redisResponse)) {
						 */
						TransactionsResponse transactionResponse = response.getBody();

						List<TransactionsData> transactions = transactionResponse.getTransactions();
						if (!CollectionUtils.isEmpty(transactions)) {
							availabletrans = transactions.size();
							if (param.getRecordLimit() < availabletrans) {
								res = setResponse(transactions, request, param, cacheKey, availabletrans, 0);

							}
						}
					}
					response = new ResponseEntity<TransactionsResponse>(res, HttpStatus.OK);
				}

			}
		}

		return response;
	}

	private ResponseEntity<TransactionsResponse> retryPriorDay(TransactionsRequest request,
			TransactionRequestParam param) {
		Integer partKey = retailPartitionCacheUtil.getParttionKey(request.getAccountInfo().getProductType(),
				request.getAccountInfo().getAccountId(), request.getAccountInfo().getProductType());
		param.setPartKey(partKey);
		ResponseEntity<TransactionsResponse> responsePriorDay = null;
		responsePriorDay = callServices("PD", request, param, true, responsePriorDay);
		LOG.debug("Retry PriorDay" + responsePriorDay.getStatusCodeValue());
		return responsePriorDay;
	}

	public ResponseEntity<TransactionsResponse> checkAndCallPriorDay(boolean blindspot, TransactionsRequest request,
			TransactionRequestParam param, ResponseEntity<TransactionsResponse> response) {
		if (blindspot) {
			// Send PD + BS
			response = callServices("PDBS", request, param, false, response);
		} else {
			// Send PD
			response = callServices("PD", request, param, false, response);
		}
		return response;
	}

	// Set Response object
	public TransactionsResponse setResponse(List<TransactionsData> transDatas, TransactionsRequest request,
			TransactionRequestParam param, String redisKey, int availabletrans, int startIndex) {
		String rediscursor = null;
		TransactionsResponse res = new TransactionsResponse();
		int endIndex = param.getRecordLimit() + startIndex;
		transDatas = transDatas.subList(startIndex, endIndex);
		res.setAccountInfo(request.getAccountInfo());
		res.setRequestId(param.getRequestId());
		ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of(TIME_ZONE));
		res.setResponseDate(date);
		res.setStatus(new Status("Success", 200));
		if (transDatas != null && !CollectionUtils.isEmpty(transDatas)) {
			res.setTransactions(transDatas);
			if (redisKey != null) {
				if (endIndex < availabletrans) {
					RecordControlResponse controlRecord = new RecordControlResponse();
					rediscursor = "RED:" + redisKey + ":" + endIndex;
					controlRecord.setCursor(rediscursor);
					res.setControlRecord(controlRecord);
				}
			}
			res.getControlRecord().setRecordSent(transDatas.size());

		}
		return res;
	}

	public ResponseEntity<TransactionsResponse> callServices(String callSerFlag, TransactionsRequest request,
			TransactionRequestParam param, boolean retryFlag, ResponseEntity<TransactionsResponse> response) {
		CompletableFuture<ResponseEntity<TransactionsResponse>> priorDayResponse = null;
		CompletableFuture<ResponseEntity<TransactionsResponse>> intraDayResponse = null;
		CompletableFuture<ResponseEntity<TransactionsResponse>> outstandingResponse = null;
		CompletableFuture<ResponseEntity<TransactionsResponse>> blindSpotResponse = null;
		ArrayList<ResponseEntity<TransactionsResponse>> responseList = new ArrayList<>();
		MDC.put(TransactionConstants.SERVICE_FLAG, MDC.get(TransactionConstants.SERVICE_FLAG) == null ? callSerFlag
				: MDC.get(TransactionConstants.SERVICE_FLAG));
		switch (callSerFlag) {
		case "OS":
			// call OS
			try {
				outstandingResponse = CompletableFuture.supplyAsync(() -> callOutstanding(request, param, callSerFlag));
				responseList.add(outstandingResponse.get());
				MDC.put(TransactionConstants.OUTSTANDING_TIME,
						String.valueOf(outstandingResponse.get().getBody().getTotalTimeTaken()));
				response = prepareResponse.prepareTransactionResponse(responseList);
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			break;
		case "PDBS":
			try {
				priorDayResponse = CompletableFuture
						.supplyAsync(() -> callPriorDay(request, param, callSerFlag, retryFlag));
				// priorDayResponse = callPriorDay(request, param, callSerFlag, retryFlag);
				blindSpotResponse = CompletableFuture.supplyAsync(() -> callBlindspot(request, param, callSerFlag));
				CompletableFuture.allOf(priorDayResponse, blindSpotResponse).join();

				responseList.add(priorDayResponse.get());
				MDC.put(TransactionConstants.PRIORDAY_TIME,
						String.valueOf(priorDayResponse.get().getBody().getTotalTimeTaken()));
				if (blindSpotResponse != null && blindSpotResponse.get().hasBody()) {
					responseList.add(blindSpotResponse.get());
					MDC.put(TransactionConstants.BLINDSPOT_TIME,
							String.valueOf(blindSpotResponse.get().getBody().getTotalTimeTaken()));
				}
				response = prepareResponse.prepareTransactionResponse(responseList);
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			break;
		case "PD":
			// call PD
			try {
				priorDayResponse = CompletableFuture
						.supplyAsync(() -> callPriorDay(request, param, callSerFlag, retryFlag));
				responseList.add(priorDayResponse.get());

				if (retryFlag) {
					MDC.put(TransactionConstants.PRIORDAY_RETRY_TIME,
							String.valueOf(priorDayResponse.get().getBody().getTotalTimeTaken()));
					MDC.put(TransactionConstants.PRIORDAY_RETRY_FLAG, Boolean.TRUE.toString());
				} else {
					MDC.put(TransactionConstants.PRIORDAY_TIME,
							String.valueOf(priorDayResponse.get().getBody().getTotalTimeTaken()));
				}
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			// response = prepareResponse.prepareTransactionResponse(responseList);
			try {
				response = priorDayResponse.get();
			} catch (InterruptedException | ExecutionException e1) {
				LOG.info(e1.toString());
			}
			break;
		case "OSPDBS":
			// Call PD OS BS
			try {
				priorDayResponse = CompletableFuture
						.supplyAsync(() -> callPriorDay(request, param, callSerFlag, retryFlag));
				outstandingResponse = CompletableFuture.supplyAsync(() -> callOutstanding(request, param, callSerFlag));
				blindSpotResponse = CompletableFuture.supplyAsync(() -> callBlindspot(request, param, callSerFlag));
				CompletableFuture.allOf(priorDayResponse, outstandingResponse, blindSpotResponse).join();
				MDC.put(TransactionConstants.OUTSTANDING_TIME,
						String.valueOf(outstandingResponse.get().getBody().getTotalTimeTaken()));
				MDC.put(TransactionConstants.PRIORDAY_TIME,
						String.valueOf(priorDayResponse.get().getBody().getTotalTimeTaken()));
				responseList.add(priorDayResponse.get());
				responseList.add(outstandingResponse.get());
				if (blindSpotResponse != null && blindSpotResponse.get().hasBody()) {
					responseList.add(blindSpotResponse.get());
					MDC.put(TransactionConstants.BLINDSPOT_TIME,
							String.valueOf(blindSpotResponse.get().getBody().getTotalTimeTaken()));
				}
				response = prepareResponse.prepareTransactionResponse(responseList);
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			break;
		case "PDOS":
			// Call PD OS
			try {
				priorDayResponse = CompletableFuture
						.supplyAsync(() -> callPriorDay(request, param, callSerFlag, retryFlag));
				outstandingResponse = CompletableFuture.supplyAsync(() -> callOutstanding(request, param, callSerFlag));
				CompletableFuture.allOf(priorDayResponse, outstandingResponse).join();
				MDC.put(TransactionConstants.OUTSTANDING_TIME,
						String.valueOf(outstandingResponse.get().getBody().getTotalTimeTaken()));
				MDC.put(TransactionConstants.PRIORDAY_TIME,
						String.valueOf(priorDayResponse.get().getBody().getTotalTimeTaken()));
				responseList.add(priorDayResponse.get());
				responseList.add(outstandingResponse.get());
				response = prepareResponse.prepareTransactionResponse(responseList);
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			break;
		case "ID":
			// Call ID
			try {
				intraDayResponse = CompletableFuture.supplyAsync(() -> callIntraDay(request, param, callSerFlag));

				responseList.add(intraDayResponse.get());
				MDC.put(TransactionConstants.INTRADAY_TIME,
						String.valueOf(intraDayResponse.get().getBody().getTotalTimeTaken()));
				response = prepareResponse.prepareTransactionResponse(responseList);
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			break;
		case "IDOS":
			// Call ID OS
			try {
				intraDayResponse = CompletableFuture.supplyAsync(() -> callIntraDay(request, param, callSerFlag));
				outstandingResponse = CompletableFuture.supplyAsync(() -> callOutstanding(request, param, callSerFlag));
				CompletableFuture.allOf(intraDayResponse, outstandingResponse).join();

				responseList.add(intraDayResponse.get());
				responseList.add(outstandingResponse.get());
				MDC.put(TransactionConstants.INTRADAY_TIME,
						String.valueOf(intraDayResponse.get().getBody().getTotalTimeTaken()));
				MDC.put(TransactionConstants.OUTSTANDING_TIME,
						String.valueOf(outstandingResponse.get().getBody().getTotalTimeTaken()));
				response = prepareResponse.prepareTransactionResponse(responseList);
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			break;
		case "IDPDBS":
			// call ID PD BD
			try {
				intraDayResponse = CompletableFuture.supplyAsync(() -> callIntraDay(request, param, callSerFlag));
				priorDayResponse = CompletableFuture
						.supplyAsync(() -> callPriorDay(request, param, callSerFlag, retryFlag));
				blindSpotResponse = CompletableFuture.supplyAsync(() -> callBlindspot(request, param, callSerFlag));
				CompletableFuture.allOf(intraDayResponse, priorDayResponse, blindSpotResponse).join();

				responseList.add(intraDayResponse.get());
				responseList.add(priorDayResponse.get());
				MDC.put(TransactionConstants.INTRADAY_TIME,
						String.valueOf(intraDayResponse.get().getBody().getTotalTimeTaken()));
				MDC.put(TransactionConstants.PRIORDAY_TIME,
						String.valueOf(priorDayResponse.get().getBody().getTotalTimeTaken()));
				if (blindSpotResponse != null && blindSpotResponse.get().hasBody()) {
					responseList.add(blindSpotResponse.get());
				}
				MDC.put(TransactionConstants.BLINDSPOT_TIME,
						String.valueOf(blindSpotResponse.get().getBody().getTotalTimeTaken()));

				response = prepareResponse.prepareTransactionResponse(responseList);
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			break;
		case "IDPD":
			// call ID PD
			try {
				intraDayResponse = CompletableFuture.supplyAsync(() -> callIntraDay(request, param, callSerFlag));
				priorDayResponse = CompletableFuture
						.supplyAsync(() -> callPriorDay(request, param, callSerFlag, retryFlag));
				CompletableFuture.allOf(intraDayResponse, priorDayResponse).join();

				responseList.add(intraDayResponse.get());
				responseList.add(priorDayResponse.get());
				MDC.put(TransactionConstants.INTRADAY_TIME,
						String.valueOf(intraDayResponse.get().getBody().getTotalTimeTaken()));
				MDC.put(TransactionConstants.PRIORDAY_TIME,
						String.valueOf(priorDayResponse.get().getBody().getTotalTimeTaken()));
				response = prepareResponse.prepareTransactionResponse(responseList);
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			break;
		case "IDOSPDBS":
			// call ID PD OS BS
			try {
				intraDayResponse = CompletableFuture.supplyAsync(() -> callIntraDay(request, param, callSerFlag));
				priorDayResponse = CompletableFuture
						.supplyAsync(() -> callPriorDay(request, param, callSerFlag, retryFlag));
				outstandingResponse = CompletableFuture.supplyAsync(() -> callOutstanding(request, param, callSerFlag));
				blindSpotResponse = CompletableFuture.supplyAsync(() -> callBlindspot(request, param, callSerFlag));
				CompletableFuture.allOf(intraDayResponse, priorDayResponse, outstandingResponse, blindSpotResponse)
						.join();

				responseList.add(intraDayResponse.get());
				responseList.add(priorDayResponse.get());
				responseList.add(outstandingResponse.get());
				if (blindSpotResponse != null && blindSpotResponse.get().hasBody()) {
					responseList.add(blindSpotResponse.get());
					MDC.put(TransactionConstants.BLINDSPOT_TIME,
							String.valueOf(blindSpotResponse.get().getBody().getTotalTimeTaken()));
				}
				MDC.put(TransactionConstants.INTRADAY_TIME,
						String.valueOf(intraDayResponse.get().getBody().getTotalTimeTaken()));
				MDC.put(TransactionConstants.PRIORDAY_TIME,
						String.valueOf(priorDayResponse.get().getBody().getTotalTimeTaken()));
				MDC.put(TransactionConstants.OUTSTANDING_TIME,
						String.valueOf(outstandingResponse.get().getBody().getTotalTimeTaken()));
				response = prepareResponse.prepareTransactionResponse(responseList);
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			break;
		case "IDOSPD":
			// call ID PD OS
			try {
				intraDayResponse = CompletableFuture.supplyAsync(() -> callIntraDay(request, param, callSerFlag));
				priorDayResponse = CompletableFuture
						.supplyAsync(() -> callPriorDay(request, param, callSerFlag, retryFlag));
				outstandingResponse = CompletableFuture.supplyAsync(() -> callOutstanding(request, param, callSerFlag));

				CompletableFuture.allOf(intraDayResponse, priorDayResponse, outstandingResponse).join();

				responseList.add(intraDayResponse.get());
				responseList.add(priorDayResponse.get());
				responseList.add(outstandingResponse.get());
				response = prepareResponse.prepareTransactionResponse(responseList);
				MDC.put(TransactionConstants.INTRADAY_TIME,
						String.valueOf(intraDayResponse.get().getBody().getTotalTimeTaken()));
				MDC.put(TransactionConstants.PRIORDAY_TIME,
						String.valueOf(priorDayResponse.get().getBody().getTotalTimeTaken()));
				MDC.put(TransactionConstants.OUTSTANDING_TIME,
						String.valueOf(outstandingResponse.get().getBody().getTotalTimeTaken()));
			} catch (CompletionException ec) {
				throw new ServiceFailedException(ec.getCause().getMessage());
			} catch (InterruptedException e) {
				LOG.info(e.toString());
			} catch (ExecutionException e) {
				LOG.info(e.toString());
			}
			break;
		case "NA":
			TransactionsResponse emptyResponse = new TransactionsResponse();
			RecordControlResponse ctrlRecord = new RecordControlResponse();
			ctrlRecord.setRecordSent(0);
			emptyResponse.setStatus(new Status("SUCCESS", 200));
			emptyResponse.setRequestId(param.getRequestId());
			emptyResponse.setAccountInfo(request.getAccountInfo());
			emptyResponse.setControlRecord(ctrlRecord);
			ZonedDateTime date = LocalDateTime.now().atZone(ZoneId.of(TIME_ZONE));
			emptyResponse.setResponseDate(date);
			response = new ResponseEntity<TransactionsResponse>(emptyResponse, HttpStatus.OK);
			if (LOG.isInfoEnabled()) {
				LOG.info(
						"Request parameters are not matching to business criteria. Gateway is unable to call any downstream service");
			}
			break;
		default:
			break;
		}
		return response;
	}

	/**
	 * Call priorday service to get priorDay transactions
	 * 
	 * @param request
	 * @param param
	 * @param serviceFlag
	 * @param retryFlag
	 * @return
	 */
	@Async
	public ResponseEntity<TransactionsResponse> callPriorDay(TransactionsRequest request, TransactionRequestParam param,
			String serviceFlag, boolean retryFlag) {
		LOG.debug("Calling PriorDay service ");
		Long startTs = System.currentTimeMillis();
		ResponseEntity<TransactionsResponse> priorDayResponse = null;
		try {
			LOG.debug("PARTKEY {}", param.getPartKey());
			priorDayResponse = priordayTxnsClient.rtltransactions(param.getOrigApp(), param.getRequestId(),
					Util.dateToString(param.getEffDate(), DT_FORMAT_YYYY_MM_DD),
					Util.dateToString(param.getInitDateTime(), DT_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z),
					param.getOperatorId(), param.getBranchId(), param.getTerminalId(),
					Util.setFlags(param.getIntradayFlag()), Util.setFlags(param.getOutstandingFlag()),
					Util.setFlags(param.getPriordayFlag()),
					Util.dateToString(param.getStartDate(), DT_FORMAT_YYYY_MM_DD),
					Util.dateToString(param.getEndDate(), DT_FORMAT_YYYY_MM_DD), param.getCreditDebitFlag(),
					param.getLowChequeNum(), param.getHighChequeNum(), param.getMinAmount(), param.getMaxAmount(),
					param.getStartTime(), param.getEndTime(), param.getDescOperator(), param.getDescSearch(),
					param.getRecordLimit(), param.getCursor(), param.getPartKey(),
					Util.dateToString(param.getLastLoadDate(), DT_FORMAT_YYYY_MM_DD), serviceFlag, request);
			Long timeTaken = System.currentTimeMillis() - startTs;
			priorDayResponse.getBody().setTotalTimeTaken(timeTaken);
			if (priorDayResponse.getBody().getStatus().getStatusCode() != 200) {
				TransactionMdcLogger.writeToMDCLog(param.getRequestId(), "FAILED PRIORDAY SERVICE", 0l,
						param.getOrigApp(), request.getAccountInfo().getProductType(), null, 0, param.getCursor(), null,
						param.getStartDate(), param.getEndDate());
				throw new ServiceFailedException("Failed to get response from priorday service");
			}
			LOG.debug("PriorDay service response received. Time taken by priorday service is : " + (timeTaken));
			LOG.info("Total transaction sent by priorday service : "
					+ priorDayResponse.getBody().getControlRecord().getRecordSent());

		} catch (Exception e) {
			TransactionMdcLogger.writeToMDCLog(param.getRequestId(), "FAILED PRIORDAY SERVICE", 0l, param.getOrigApp(),
					request.getAccountInfo().getProductType(), null, 0, param.getCursor(), null, param.getStartDate(),
					param.getEndDate());
			throw new ServiceFailedException("Failed to get response from priorday service");
		}
		return priorDayResponse;
	}

	/**
	 * Call Blind spot service if Next Proceesing date -1 is lesser then request's
	 * End Date
	 * 
	 * @param request
	 * @param param
	 * @param serviceFlag
	 * @return
	 */
	@Async
	public ResponseEntity<TransactionsResponse> callBlindspot(TransactionsRequest request,
			TransactionRequestParam param, String serviceFlag) {
		LOG.debug("Calling Blindspot service ");
		ResponseEntity<TransactionsResponse> blindSpotResponse = null;
		Long startTs = System.currentTimeMillis();
		try {
			blindSpotResponse = blindspotTxnsClient.rtltransactions(param.getOrigApp(), param.getRequestId(),
					Util.dateToString(param.getEffDate(), DT_FORMAT_YYYY_MM_DD),
					Util.dateToString(param.getInitDateTime(), DT_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z),
					param.getOperatorId(), param.getBranchId(), param.getTerminalId(),
					Util.setFlags(param.getIntradayFlag()), Util.setFlags(param.getOutstandingFlag()),
					Util.setFlags(param.getPriordayFlag()),
					Util.dateToString(param.getStartDate(), DT_FORMAT_YYYY_MM_DD),
					Util.dateToString(param.getEndDate(), DT_FORMAT_YYYY_MM_DD), param.getCreditDebitFlag(),
					param.getLowChequeNum(), param.getHighChequeNum(), param.getMinAmount(), param.getMaxAmount(),
					param.getStartTime(), param.getEndTime(), param.getDescOperator(), param.getDescSearch(),
					param.getRecordLimit(), param.getCursor(), param.getPartKey(),
					Util.dateToString(param.getNextLoadDate(), DT_FORMAT_YYYY_MM_DD), serviceFlag, request);
			Long timeTaken = System.currentTimeMillis() - startTs;
			blindSpotResponse.getBody().setTotalTimeTaken(timeTaken);
			if (blindSpotResponse.getBody().getStatus().getStatusCode() != 200) {
				// Story : https://jira.service.anz/browse/CISP-2383
				if (enablePartialSuccess.equals("Y")) {
					blindSpotResponse.getBody().getStatus().setStatusCode(206);
					blindSpotResponse.getBody().getStatus().setDescription("PARTIAL SUCCESS");
					blindSpotResponse.getBody().setTransactions(null);
				} else {
					TransactionMdcLogger.writeToMDCLog(param.getRequestId(), "FAILED BLINDSPOT SERVICE", 0l,
							param.getOrigApp(), request.getAccountInfo().getProductType(), null, 0, param.getCursor(),
							null, param.getStartDate(), param.getEndDate());
					throw new ServiceFailedException("Failed to get response from blindspot service");
				}
			}
			LOG.debug("BlindSpot service response received. Time taken by blindspot service is : " + (timeTaken));
			LOG.info("Total transaction sent by blindspot service : "
					+ blindSpotResponse.getBody().getControlRecord().getRecordSent());
		} catch (Exception e) {
			if (enablePartialSuccess.equals("Y")) {
				TransactionsResponse res = new TransactionsResponse();
				res.setAccountInfo(request.getAccountInfo());
				blindSpotResponse = new ResponseEntity<TransactionsResponse>(res, HttpStatus.PARTIAL_CONTENT);
				blindSpotResponse.getBody().setStatus(new Status("PARTIAL SUCCESS", 206));
				blindSpotResponse.getBody().setTransactions(null);
			}
		}

		return blindSpotResponse;
	}

	/**
	 * Call Intraday service to get Current day trasactions
	 * 
	 * @param request
	 * @param param
	 * @param serviceFlag
	 * @return
	 */
	@Async
	public ResponseEntity<TransactionsResponse> callIntraDay(TransactionsRequest request, TransactionRequestParam param,
			String serviceFlag) {

		LOG.debug("Calling Intraday service ");
		Long startTs = System.currentTimeMillis();
		ResponseEntity<TransactionsResponse> intraDayResponse = null;
		try {
			intraDayResponse = intraDayTxnClient.rtltransactions(param.getOrigApp(), param.getRequestId(),
					Util.dateToString(param.getEffDate(), DT_FORMAT_YYYY_MM_DD),
					Util.dateToString(param.getInitDateTime(), DT_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z),
					param.getOperatorId(), param.getBranchId(), param.getTerminalId(),
					Util.setFlags(param.getIntradayFlag()), Util.setFlags(param.getOutstandingFlag()),
					Util.setFlags(param.getPriordayFlag()),
					Util.dateToString(param.getStartDate(), DT_FORMAT_YYYY_MM_DD),
					Util.dateToString(param.getEndDate(), DT_FORMAT_YYYY_MM_DD), param.getCreditDebitFlag(),
					param.getLowChequeNum(), param.getHighChequeNum(), param.getMinAmount(), param.getMaxAmount(),
					Util.timeToString(param.getStartTime()), Util.timeToString(param.getEndTime()),
					param.getDescOperator(), param.getDescSearch(), param.getRecordLimit(), param.getCursor(),
					param.getPartKey(), Util.dateToString(param.getLastLoadDate(), DT_FORMAT_YYYY_MM_DD), serviceFlag,
					request);
			Long timeTaken = System.currentTimeMillis() - startTs;
			intraDayResponse.getBody().setTotalTimeTaken(timeTaken);
			if (intraDayResponse.getBody().getStatus().getStatusCode() != 200) {
				TransactionMdcLogger.writeToMDCLog(param.getRequestId(), "FAILED INTRADAY SERVICE", 0l,
						param.getOrigApp(), request.getAccountInfo().getProductType(), null, 0, param.getCursor(), null,
						param.getStartDate(), param.getEndDate());
				throw new ServiceFailedException("Failed to get response from intraday service");
			}
			LOG.debug("Intraday service response received. Time taken by intraday service is : " + (timeTaken));
			LOG.info("Total transaction sent by intraday service : "
					+ intraDayResponse.getBody().getControlRecord().getRecordSent());
		} catch (Exception e) {
			TransactionMdcLogger.writeToMDCLog(param.getRequestId(), "FAILED INTRADAY SERVICE", 0l, param.getOrigApp(),
					request.getAccountInfo().getProductType(), null, 0, param.getCursor(), null, param.getStartDate(),
					param.getEndDate());
			throw new ServiceFailedException("Failed to get response from intraday service");
		}
		return intraDayResponse;
	}

	/**
	 * Call Transaction enrichment service to get LWC detail for merchants
	 * 
	 * @param request TransactionsRequest
	 * @param param   TransactionRequestParam
	 * @param req     EnrichTransactionRequest
	 * @return
	 */
	public ResponseEntity<EnrichTransactionResponse> rtlEnrichTxnsService(TransactionsRequest request,
			TransactionRequestParam param, EnrichTransactionRequest req) {

		LOG.debug("Calling Retail EnrichTxns service ");
		Long startTs = System.currentTimeMillis();
		ResponseEntity<EnrichTransactionResponse> enrichResponse = null;

		try {
			enrichResponse = enrichTxnClient.enrichTransactions(param.getRequestId(),
					Util.dateToString(param.getInitDateTime(), DT_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z), SRC_APP, req);

			Long timeTaken = System.currentTimeMillis() - startTs;
			MDC.put(TransactionConstants.TRANSACTION_ENRICH, String.valueOf(timeTaken));
			MDC.put(TransactionConstants.MERCHANT_INFO_FLG, String.valueOf(param.getIncludeMerchantInfo()));
			if (enrichResponse.getBody().getStatus().getStatusCode() != 200) {
				TransactionMdcLogger.writeToMDCLog(param.getRequestId(), "FAILED EnrichTxns SERVICE", 0l,
						param.getOrigApp(), request.getAccountInfo().getProductType(), null, 0, param.getCursor(), null,
						param.getStartDate(), param.getEndDate());
				throw new ServiceFailedException("Failed to get response from EnrichTxns service");
			}
			LOG.debug("Transaction Enrichment service response received. Time taken by EnrichTxns service is : "
					+ (timeTaken));
			LOG.info("Total transaction sent by enrich service : "
					+ (enrichResponse.getBody().getMerchantData().size()));
		} catch (Exception e) {
			LOG.error("ERROR FAILED Enrich SERVICE:", e);
			TransactionMdcLogger.writeToMDCLog(param.getRequestId(), "FAILED Enrich SERVICE", 0l, param.getOrigApp(),
					request.getAccountInfo().getProductType(), null, 0, param.getCursor(), null, param.getStartDate(),
					param.getEndDate());
			// throw new ServiceFailedException("Failed to get response from Enrich
			// service");
		}
		return enrichResponse;
	}

	/**
	 * Call Outstanding service to get Outstanding Transactions from CIM
	 * 
	 * @param request
	 * @param param
	 * @param serviceFlag
	 * @return
	 */
	@Async
	public ResponseEntity<TransactionsResponse> callOutstanding(TransactionsRequest request,
			TransactionRequestParam param, String serviceFlag) {
		LOG.debug("Calling OutStanding service ");
		Long startTs = System.currentTimeMillis();
		ResponseEntity<TransactionsResponse> outStandingResponse = null;
		try {
			outStandingResponse = outStandingTxnClient.rtltransactions(param.getOrigApp(), param.getRequestId(),
					Util.dateToString(param.getEffDate(), DT_FORMAT_YYYY_MM_DD),
					Util.dateToString(param.getInitDateTime(), DT_FORMAT_YYYY_MM_DD_T_HH_MM_SS_Z),
					param.getOperatorId(), param.getBranchId(), param.getTerminalId(),
					Util.setFlags(param.getIntradayFlag()), Util.setFlags(param.getOutstandingFlag()),
					Util.setFlags(param.getPriordayFlag()),
					Util.dateToString(param.getStartDate(), DT_FORMAT_YYYY_MM_DD),
					Util.dateToString(param.getEndDate(), DT_FORMAT_YYYY_MM_DD), param.getCreditDebitFlag(),
					param.getLowChequeNum(), param.getHighChequeNum(), param.getMinAmount(), param.getMaxAmount(),
					Util.timeToString(param.getStartTime()), Util.timeToString(param.getEndTime()),
					param.getDescOperator(), param.getDescSearch(), param.getRecordLimit(), param.getCursor(),
					param.getPartKey(), Util.dateToString(param.getLastLoadDate(), DT_FORMAT_YYYY_MM_DD), serviceFlag,
					request);
			Long timeTaken = System.currentTimeMillis() - startTs;
			if (outStandingResponse.getBody().getStatus().getStatusCode() != 200) {
				TransactionMdcLogger.writeToMDCLog(param.getRequestId(), "FAILED OUTSTANDING SERVICE", 0l,
						param.getOrigApp(), request.getAccountInfo().getProductType(), null, 0, param.getCursor(), null,
						param.getStartDate(), param.getEndDate());
				throw new ServiceFailedException("Failed to get response from outstanding service");
			}
			outStandingResponse.getBody().setTotalTimeTaken(timeTaken);
			LOG.debug("OutStanding service response received. Time taken by outstanding service is : " + (timeTaken));
			LOG.info("Total transaction sent by outstanding service : "
					+ outStandingResponse.getBody().getControlRecord().getRecordSent());
		} catch (Exception e) {
			TransactionMdcLogger.writeToMDCLog(param.getRequestId(), "FAILED OUTSTANDING SERVICE", 0l,
					param.getOrigApp(), request.getAccountInfo().getProductType(), null, 0, param.getCursor(), null,
					param.getStartDate(), param.getEndDate());
			throw new ServiceFailedException("Failed to get response from outstanding service");
		}
		return outStandingResponse;
	}
}
