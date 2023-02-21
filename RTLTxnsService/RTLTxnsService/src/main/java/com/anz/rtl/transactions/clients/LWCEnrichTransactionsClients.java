package com.anz.rtl.transactions.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.anz.rtl.transactions.FeignClientConfiguration;
import com.anz.rtl.transactions.request.EnrichTransactionRequest;
import com.anz.rtl.transactions.response.EnrichTransactionResponse;

@FeignClient(value = "${enrichtxns.service.name}", url = "${enrichtxns.service.url}", configuration = FeignClientConfiguration.class)
public interface LWCEnrichTransactionsClients {
	@PostMapping(value = "/v2/enrich/transactions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<EnrichTransactionResponse> enrichTransactions(
			@RequestHeader(value = "X-Request-Id") String requestId,
			@RequestHeader(value =  "X-Init-DateTime")String initDateTime,
			@RequestHeader(value = "X-Source-App" )String sourceApp,
			@RequestBody EnrichTransactionRequest request
			);
}
