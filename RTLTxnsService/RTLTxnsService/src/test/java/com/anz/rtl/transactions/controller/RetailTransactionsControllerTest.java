package com.anz.rtl.transactions.controller;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.anz.rtl.transactions.dao.RedisTransactionRepository;
import com.anz.rtl.transactions.request.TransactionAccountDetailRequest;
import com.anz.rtl.transactions.request.TransactionRequestParam;
import com.anz.rtl.transactions.request.TransactionsRequest;
import com.anz.rtl.transactions.response.HandleMethodArgumentNotValid;
import com.anz.rtl.transactions.response.RecordControlResponse;
import com.anz.rtl.transactions.response.TransactionsData;
import com.anz.rtl.transactions.response.TransactionsResponse;
import com.anz.rtl.transactions.service.CacheService;
import com.anz.rtl.transactions.service.RetailTransactionService;
import com.anz.rtl.transactions.util.ServiceFailedException;
import com.anz.rtl.transactions.util.SessionExpiredException;
import com.anz.rtl.transactions.util.TransactionMdcLogger;
import com.anz.rtl.transactions.util.ValidationException;

@RunWith(SpringRunner.class)
@WebMvcTest
@PropertySource("classpath:application.properties")
public class RetailTransactionsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	TransactionsResponse response;
	@MockBean
	TransactionsRequest request;
	@MockBean
	TransactionMdcLogger transactionMdcLogger;

	@MockBean
	private RetailTransactionService retailTxnService;

	@MockBean
	RedisTransactionRepository redisTransactionRepository;

	@MockBean
	TransactionRequestParam param;

	@Mock
	TransactionAccountDetailRequest accountInfo;

	@MockBean
	private CacheService cacheService;

	@Test
	public void testRtlTransactionsForResponseOK() throws Exception {
		// ResultMatcher OK = MockMvcResultMatchers.status().isOk();

		Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(TransactionRequestParam.class),
				Mockito.any(TransactionsRequest.class), Mockito.any())).thenReturn(response());
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "THA");
		httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "2020-11-30").param("intraday", "false")
				.param("priorday", "true").param("outstanding", "true").param("start-date", "2018-07-01")
				.param("include-merchant-info", "false")
				.content("{\r\n" + "    \"accountInfo\": {\r\n" + "        \"systemCode\": \"VIS\",\r\n"
						+ "        \"countryCode\": \"AU\",\r\n" + "        \"currencyCode\": \"AUD\",\r\n"
						+ "        \"accountId\": \"4072200015623730\",\r\n" + "        \"productType\": \"PC\",\r\n"
						+ "        \"custPermId\": 314944351\r\n" + "    }\r\n" + "}")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk());
		// mockMvc.perform(requestBuilder).andDo(print());
	}

	@Test
	public void testRtlTransactionsForResponseForInvalidDate() throws Exception {
		// ResultMatcher OK = MockMvcResultMatchers.status().isOk();
		try {
			Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(TransactionRequestParam.class),
					Mockito.any(TransactionsRequest.class), Mockito.any()))
					.thenThrow(MethodArgumentTypeMismatchException.class);
		} catch (MethodArgumentTypeMismatchException e) {

		}

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "THA");
		httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "30-11-2020").param("intraday", "false")
				.param("priorday", "true").param("outstanding", "true").param("start-date", "01-07-2018")
				.param("include-merchant-info", "false")
				.content("{\r\n" + "    \"accountInfo\": {\r\n" + "        \"systemCode\": \"VIS\",\r\n"
						+ "        \"countryCode\": \"AU\",\r\n" + "        \"currencyCode\": \"AUD\",\r\n"
						+ "        \"accountId\": \"4072200015623730\",\r\n" + "        \"productType\": \"PC\",\r\n"
						+ "        \"custPermId\": 314944351\r\n" + "    }\r\n" + "}")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isBadRequest());
		// mockMvc.perform(requestBuilder).andDo(print());
	}

	@Test
	public void testRtlTransactionsForResponse() throws Exception {
		ResultMatcher error = MockMvcResultMatchers.status().isBadRequest();

		// Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(),
		// Mockito.any(), Mockito.any()))
		// .thenReturn(null);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "THA");
		httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "30-11-2020").param("intraday", "true")
				.param("priorday", "false").param("outstanding", "false").param("start-date", "01-07-2018")
				.param("include-merchant-info", "true")
				.content("{\r\n" + "    \"accountInfo\": {\r\n" + "        \"systemCode\": \"VIS\",\r\n"
						+ "        \"countryCode\": \"AU\",\r\n" + "        \"currencyCode\": \"AUD\",\r\n"
						+ "        \"accountId\": \"4072200015623730\",\r\n" + "        \"productType\": \"PC\",\r\n"
						+ "        \"custPermId\": 314944351\r\n" + "    }\r\n" + "}")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(error);
		// mockMvc.perform(requestBuilder).andDo(print());
	}

	private ResponseEntity<TransactionsResponse> response() {
		TransactionsResponse res = new TransactionsResponse();
		RecordControlResponse res1 = new RecordControlResponse();
		res1.setRecordSent(1);
		res.setControlRecord(res1);
		res.setRequestId("1");
		// res.setStatus(Status.);
		List<TransactionsData> data = new ArrayList<TransactionsData>();
		// data.setTransactionId("1");

		res.setTransactions(data);
		ResponseEntity<TransactionsResponse> response = new ResponseEntity<TransactionsResponse>(res, HttpStatus.OK);

		return response;

	}

	@Test
	public void testRtlTransactionsForResponseForMissingRequestHeaders() throws Exception {
		try {
			Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(TransactionRequestParam.class),
					Mockito.any(TransactionsRequest.class), Mockito.any()))
					.thenThrow(MissingRequestHeaderException.class);
		} catch (Exception e) {

		}

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "THA");
		// httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "2020-11-30").param("intraday", "false")
				.param("priorday", "true").param("outstanding", "true").param("start-date", "2018-07-01")
				.param("include-merchant-info", "false")
				.content("{\r\n" + "    \"accountInfo\": {\r\n" + "        \"systemCode\": \"VIS\",\r\n"
						+ "        \"countryCode\": \"AU\",\r\n" + "        \"currencyCode\": \"AUD\",\r\n"
						+ "        \"accountId\": \"4072200015623730\",\r\n" + "        \"productType\": \"PC\",\r\n"
						+ "        \"custPermId\": 314944351\r\n" + "    }\r\n" + "}")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	public void testRtlTransactionsForResponseForSessionTimeOut() throws Exception {
		try {
			Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(TransactionRequestParam.class),
					Mockito.any(TransactionsRequest.class), Mockito.any()))
					.thenThrow(SessionExpiredException.class);
		} catch (SessionExpiredException e) {

		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "THA");
		httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "2020-11-30").param("intraday", "false")
				.param("priorday", "true").param("outstanding", "true").param("start-date", "2018-07-01")
				.param("include-merchant-info", "false")
				.content("{\r\n" + "    \"accountInfo\": {\r\n" + "        \"systemCode\": \"VIS\",\r\n"
						+ "        \"countryCode\": \"AU\",\r\n" + "        \"currencyCode\": \"AUD\",\r\n"
						+ "        \"accountId\": \"4072200015623730\",\r\n" + "        \"productType\": \"PC\",\r\n"
						+ "        \"custPermId\": 314944351\r\n" + "    }\r\n" + "}")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isRequestTimeout());
		// mockMvc.perform(requestBuilder).andDo(print());
	}
	
	@Test
	public void testRtlTransactionsForResponseForDateValidalidations() throws Exception {
		try {
			Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(TransactionRequestParam.class),
					Mockito.any(TransactionsRequest.class), Mockito.any()))
					.thenThrow(ValidationException.class);
		} catch (ValidationException e) {

		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "THA");
		httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "2020-07-20").param("intraday", "false")
				.param("priorday", "true").param("outstanding", "true").param("start-date", "2018-07-01")
				.param("include-merchant-info", "false")
				.content("{\r\n" + "    \"accountInfo\": {\r\n" + "        \"systemCode\": \"VIS\",\r\n"
						+ "        \"countryCode\": \"AU\",\r\n" + "        \"currencyCode\": \"AUD\",\r\n"
						+ "        \"accountId\": \"4072200015623730\",\r\n" + "        \"productType\": \"PC\",\r\n"
						+ "        \"custPermId\": 314944351\r\n" + "    }\r\n" + "}")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isInternalServerError());
		// mockMvc.perform(requestBuilder).andDo(print());
	}
	@Test
	public void testRtlTransactionsForResponseForAllExceptions() throws Exception {
		try {
			Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(TransactionRequestParam.class),
					Mockito.any(TransactionsRequest.class), Mockito.any()))
					.thenThrow(Exception.class);
		} catch (Exception e) {

		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "THA");
		httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "2020-07-20").param("intraday", "false")
				.param("priorday", "true").param("outstanding", "true").param("start-date", "2018-07-01")
				.param("include-merchant-info", "false")
				.content("{\r\n" + "    \"accountInfo\": {\r\n" + "        \"systemCode\": \"VIS\",\r\n"
						+ "        \"countryCode\": \"AU\",\r\n" + "        \"currencyCode\": \"AUD\",\r\n"
						+ "        \"accountId\": \"4072200015623730\",\r\n" + "        \"productType\": \"PC\",\r\n"
						+ "        \"custPermId\": 314944351\r\n" + "    }\r\n" + "}")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isInternalServerError());
		// mockMvc.perform(requestBuilder).andDo(print());
	}

	@Test
	public void testRtlTransactionsForResponseForServiceFailedException() throws Exception {
		try {
			Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(TransactionRequestParam.class),
					Mockito.any(TransactionsRequest.class), Mockito.any()))
					.thenThrow(ServiceFailedException.class);
		} catch (ServiceFailedException e) {

		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "THA");
		httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "2020-07-20").param("intraday", "false")
				.param("priorday", "true").param("outstanding", "true").param("start-date", "2018-07-01")
				.param("include-merchant-info", "false")
				.content("{\r\n" + "    \"accountInfo\": {\r\n" + "        \"systemCode\": \"VIS\",\r\n"
						+ "        \"countryCode\": \"AU\",\r\n" + "        \"currencyCode\": \"AUD\",\r\n"
						+ "        \"accountId\": \"4072200015623730\",\r\n" + "        \"productType\": \"PC\",\r\n"
						+ "        \"custPermId\": 314944351\r\n" + "    }\r\n" + "}")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
		// mockMvc.perform(requestBuilder).andDo(print());
	}

	@Test
	public void testRtlTransactionsForResponseForHandleMethodArgumentNotValid() throws Exception {
		try {
			Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(TransactionRequestParam.class),
					Mockito.any(TransactionsRequest.class), Mockito.any()))
					.thenThrow(HandleMethodArgumentNotValid.class);
		} catch (HandleMethodArgumentNotValid e) {

		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "THA");
		httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "2020-07-20").param("intraday", "false")
				.param("priorday", "true").param("outstanding", "true").param("start-date", "2018-07-01")
				.param("include-merchant-info", "false")
				.content("{\r\n" + 
						"  \"accountInfo\": {\r\n" + 
						"    \"systemCode\": \"VIS\",\r\n" + 
						"    \"countryCode\": \"AU\",\r\n" + 
						"    \"currencyCode\": \"AUD\",\r\n" + 
						"    \"accountId\": \"4560045020158084\",\r\n" + 
						"    \"custPermId\": 314944351\r\n" + 
						"  }\r\n" + 
						"}")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isBadRequest());
		// mockMvc.perform(requestBuilder).andDo(print());
	}
	@Test
	public void testRtlTransactionsForResponseForConstraintViolationException() throws Exception {
		try {
			Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(TransactionRequestParam.class),
					Mockito.any(TransactionsRequest.class), Mockito.any()))
					.thenThrow(ConstraintViolationException.class);
		} catch (ConstraintViolationException e) {

		}
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "THA");
		httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "2020-07-20").param("intraday", "false")
				.param("priorday", "true").param("outstanding", "true").param("start-date", "2018-07-01")
				.param("include-merchant-info", "false")
				.content("{\r\n" + 
						"  \"accountInfo\": {\r\n" + 
						"    \"systemCode\": \"VIS\",\r\n" + 
						"    \"countryCode\": \"AU\",\r\n" + 
						"    \"currencyCode\": \"AUD\",\r\n" + 
						"    \"accountId\": \"null\",\r\n" + 
						"    \"productType\": \"null\",\r\n" + 
						"    \"custPermId\": 314944351\r\n" + 
						"  }\r\n" + 
						"} ")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isBadRequest());
		// mockMvc.perform(requestBuilder).andDo(print());
	}
	
	@Test
	public void testTransactionsForResponseOK() throws Exception {
		// ResultMatcher OK = MockMvcResultMatchers.status().isOk();

		Mockito.when(retailTxnService.flowDecisionProcessor(Mockito.any(TransactionRequestParam.class),
				Mockito.any(TransactionsRequest.class), Mockito.any())).thenReturn(response());
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("X-Orig-App", "PPA");
		httpHeaders.add("X-Request-Id", "df49cd3e-11bc-4e85-a63c-b1e680e1xEk9");
		httpHeaders.add("X-Effective-Date", "2019-07-05");
		httpHeaders.add("X-Init-DateTime", "2019-07-05T18:46:19Z");
		httpHeaders.add("X-Operator-Id", "finacleu");
		httpHeaders.add("X-Branch-Id", "003026");
		httpHeaders.add("X-Session-TimeOut", "5000");
		httpHeaders.add("X-Terminal-Id", "123");
		httpHeaders.add("Accept", "application/json");
		httpHeaders.add("Content-Type", "application/json");
		// param = MapRequestParam();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/accounts/transactions").headers(httpHeaders)
				.param("record-limit", "10").param("end-date", "2020-11-30").param("intraday", "true")
				.param("priorday", "false").param("outstanding", "false").param("start-date", "2018-07-01")
				.param("include-merchant-info", "false")
				.content("{\r\n" + "    \"accountInfo\": {\r\n" + "        \"systemCode\": \"VIS\",\r\n"
						+ "        \"countryCode\": \"AU\",\r\n" + "        \"currencyCode\": \"AUD\",\r\n"
						+ "        \"accountId\": \"4072200015623730\",\r\n" + "        \"productType\": \"PC\",\r\n"
						+ "        \"custPermId\": 314944351\r\n" + "    }\r\n" + "}")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

		mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk());
		// mockMvc.perform(requestBuilder).andDo(print());
	}


}
