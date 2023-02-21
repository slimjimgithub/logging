package com.anz.rtl.transactions.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.anz.rtl.transactions.request.TransactionAccountDetailRequest;
import com.anz.rtl.transactions.response.AdditionalMerchantDetails;
import com.anz.rtl.transactions.response.EnrichTransactionResponse;
import com.anz.rtl.transactions.response.RecordControlResponse;
import com.anz.rtl.transactions.response.Status;
import com.anz.rtl.transactions.response.TransactionsData;
import com.anz.rtl.transactions.response.TransactionsResponse;

@RunWith(MockitoJUnitRunner.class)
public class PrepareResponseTest {

	private static final Logger LOG = LoggerFactory.getLogger(RetailTransactionServiceTest.class);

	@InjectMocks
	PrepareResponse prepareResponse;

	@Test
	public void prepareTransactionResponseTest() {
		List<ResponseEntity<TransactionsResponse>> responseList = new ArrayList<ResponseEntity<TransactionsResponse>>();
		responseList.add(createResponseWithTxn());
		prepareResponse.prepareTransactionResponse(responseList);
		// assertEquals(createResponseWithTxn(),prepareResponse.prepareTransactionResponse(responseList));
	}

	@Test
	public void prepareTransactionResponseTestWhenResponseListIsEmpty() {
		List<ResponseEntity<TransactionsResponse>> responseList = new ArrayList<ResponseEntity<TransactionsResponse>>();
		responseList.add(createEmptyResponse());
		prepareResponse.prepareTransactionResponse(responseList);
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

	public ResponseEntity<TransactionsResponse> createEmptyResponse() {
		TransactionsResponse response = new TransactionsResponse();
		/*
		 * response.setRequestId("54321");
		 * response.setResponseDate(stringToDate("2019-05-06", "yyyy-MM-dd"));
		 * response.setStatus(new Status("Success", 200));
		 * response.setAccountInfo(setAccInfo());
		 * response.setControlRecord(setRecordResponse());
		 * response.setTransactions(txnList());
		 */
		response.setStatus(new Status("PartialSuccess", 206));
		ResponseEntity<TransactionsResponse> responseEntity = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);
		return responseEntity;
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
		recResponse.setCursor("RED");
		return recResponse;
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
		transactionsData.setSortingDate(stringToDate("2018-02-06", "yyyy-MM-dd"));
		transactionsData.setSortingTransStatus(1);
		transactionsData.setDesc4("");
		transactionsData.setDesc3("");
		TransactionsData transactionsData1 = new TransactionsData();
		String date = "2016-08-22 14:30";
        LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        ZonedDateTime klDateTime = ldt.atZone(ZoneId.of("Asia/Kuala_Lumpur"));
		transactionsData1.setOriginDateTime(klDateTime);
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
		transactionsData1.setSortingDate(stringToDate("2018-03-06", "yyyy-MM-dd"));
		transactionsData1.setSortingTransStatus(1);

		transactions.add(transactionsData);
		transactions.add(transactionsData1);

		return transactions;
	}

	@Test
	public void testMergeLWCEnrichResponse() {
		EnrichTransactionResponse res = new EnrichTransactionResponse();
		TransactionsResponse response = new TransactionsResponse();
		List<AdditionalMerchantDetails> det = new ArrayList<AdditionalMerchantDetails>();
		// AdditionalMerchantDetails det = new AdditionalMerchantDetails();
		AdditionalMerchantDetails obj = new AdditionalMerchantDetails();
		obj.setMerchantId("d2d5718e-5369-41f8-bb98-a1020da38416");
		obj.setMerchantPrimaryName("TREND MICRO AUSTRALIA");
		obj.setPrimaryCategory("SHOPPING");
		obj.setSuburb("North Sydney");
		obj.setState("KFC EARLWO");
		obj.setTxnEnrichmentId("");
		obj.setCal("TREND MICRO AUSTRALIA");
		det.add(obj);
		List<TransactionsData> data = new ArrayList<TransactionsData>();
		TransactionsData obj1 = new TransactionsData();
		obj1.setAliasName("ANZ NPP VERIFICATION ACCOUNT");
		obj1.setCal("TREND MICRO AUSTRALIA");
		data.add(obj1);
		Date responseDate = stringToDate("2018-11-01", "yyyy-MM-dd");
		res.setRequestId("");
		res.setResponseDate(responseDate);
		res.setMerchantData(det);
		response.setTransactions(data);

		ResponseEntity<EnrichTransactionResponse> enrichResponse = new ResponseEntity<EnrichTransactionResponse>(res,
				HttpStatus.OK);
		ResponseEntity<TransactionsResponse> respons = new ResponseEntity<TransactionsResponse>(response,
				HttpStatus.OK);
		ResponseEntity<TransactionsResponse> re = prepareResponse.mergeLWCEnrichResponse(enrichResponse, respons, "");
		assertEquals(HttpStatus.OK, re.getStatusCode());
	}

}
