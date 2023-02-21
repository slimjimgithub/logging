package com.anz.rtl.transactions.clients;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.anz.rtl.transactions.FeignClientConfiguration;
import com.anz.rtl.transactions.request.TransactionsRequest;
import com.anz.rtl.transactions.response.TransactionsResponse;

@FeignClient(value = "${outstandingtxns.service.name}", url = "${outstandingtxns.service.url}", configuration = FeignClientConfiguration.class)
public interface OutstandingTransactionsClient {
	@GetMapping(value = "/outstanding/transactions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<TransactionsResponse> rtltransactions(@RequestHeader(value = "X-Orig-App") String origApp,
			@RequestHeader(value = "X-Request-Id") String requestId,
			@RequestHeader(value = "X-Effective-Date") String effDate,
			@RequestHeader(value = "X-Init-DateTime") String initDateTime,
			@RequestHeader(value = "X-Operator-Id") String operatorId,
			@RequestHeader(value = "X-Branch-Id") Integer branchId,
			@RequestHeader(value = "X-Terminal-Id") String terminalId,
			@RequestParam(value = "intraday", required = false, defaultValue = "true") Boolean intradayFlag,
			@RequestParam(value = "outstanding", required = false, defaultValue = "true") Boolean outstandingFlag,
			@RequestParam(value = "priorday", required = false, defaultValue = "true") Boolean priordayFlag,
			@RequestParam(value = "start-date", required = false) String startDate,
			@RequestParam(value = "end-date", required = false) String endDate,
			/*
			 * @RequestParam(value = "debit", required = false, defaultValue = "false")
			 * Boolean debitFlag,
			 * 
			 * @RequestParam(value = "credit", required = false, defaultValue = "false")
			 * Boolean creditFlag,
			 */
			@RequestParam(value = "credit-debit-flag", required = false) String creditDebitFlag,
			@RequestParam(value = "low-cheque-num", required = false, defaultValue = "0") Integer lowChequeNum,
			@RequestParam(value = "high-cheque-num", required = false, defaultValue = "0") Integer highChequeNum,
			@RequestParam(value = "min-amount", required = false) BigDecimal minAmount,
			@RequestParam(value = "max-amount", required = false) BigDecimal maxAmount,
			@RequestParam(value = "start-time", required = false) String startTime,
			@RequestParam(value = "end-time", required = false) String endTime,
			@RequestParam(value = "desc-operator", required = false) String descOperator,
			@RequestParam(value = "desc-search", required = false) String descSearch,
			@RequestParam(value = "record-limit", required = true) Integer recordLimit,
			@RequestParam(value = "cursor", required = false) String cursor,
			@RequestParam(value = "part-key", required = false) Integer partKey,
			@RequestParam(value = "last-load-date", required = false) String lastLoadDate,
			@RequestParam(value = "service-flag", required = false) String serviceFlag,
			@RequestBody TransactionsRequest request);
}
