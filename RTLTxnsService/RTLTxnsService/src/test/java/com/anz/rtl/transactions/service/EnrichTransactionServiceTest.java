package com.anz.rtl.transactions.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.anz.rtl.transactions.request.TransactionAccountDetailRequest;
import com.anz.rtl.transactions.request.TransactionsRequest;
import com.anz.rtl.transactions.response.TransactionsData;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EnrichTransactionServiceTest {

	@InjectMocks
	EnrichTransactionService enrichTxnService;
	@Mock
	TransactionsRequest request;
	@Mock
	TransactionAccountDetailRequest accountInfo;
	@Mock
	TransactionsData transactions;
	@Test
	public void testGetEligibleCalsforcase1PCfornullsortingstatus() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setMerchantName("AUS");
		//data1.setSortingTransStatus(1);
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTONAU", "PLUMTONAU");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsforcase1PC() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setMerchantName("AUS");
		data1.setSortingTransStatus(1);
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTONAU", "PLUMTONAU");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsforcase1PCfor() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setMerchantName(null);
		data1.setSortingTransStatus(1);
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTONAU", "PLUMTONAU");
		try {
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
		}catch(Exception e) {
			
		}
	}
	@Test
	public void testGetEligibleCalsforCase1PC() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setMerchantName("PAYPAL");
		data1.setSortingTransStatus(1);
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTONAU", "PLUMTONAU");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase1PC() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setMerchantName("AU");
		data1.setSortingTransStatus(1);
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTONAU", "PLUMTONAU");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsforCase2PC021() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("021");
		data1.setDesc2(null);
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsforCase2PC02() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("02");
		data1.setDesc2(null);
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsforCase2PC() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("2");
		data1.setDesc2("PLUMTON");
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligiblecalsForCase2PC() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("6");
		data1.setTransactionCode("48");
		data1.setDesc2("PLUMTON");
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase2PC() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("06");
		data1.setTransactionCode("09");
		data1.setDesc2("PLUMTON");
		data1.setAmount(new BigDecimal(105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase2PCfortxntype48() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("48");
		data1.setTransactionCode("09");
		data1.setDesc2("PLUMTON");
		data1.setAmount(new BigDecimal(105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase2PCfortxntype9() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("9");
		data1.setTransactionCode("09");
		data1.setDesc2("PLUMTON");
		data1.setAmount(new BigDecimal(105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase2PCfortxntype09() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("06");
		data1.setTransactionCode("09");
		data1.setDesc2("PLUMTON");
		data1.setAmount(new BigDecimal(105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase2PCfortxntype18() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("18");
		data1.setTransactionCode("09");
		data1.setDesc2("PLUMTON");
		data1.setAmount(new BigDecimal(105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase2PCfortxntype22() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("22");
		data1.setTransactionCode("09");
		data1.setDesc2("PLUMTON");
		data1.setAmount(new BigDecimal(105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase3PCfornulltxns() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("C");
		data1.setTransactionCode("26");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee("");
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase3PCfornulltxncode() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		//data1.setTransactionType("D");
		data1.setTransactionCode(null);
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee("");
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsforCase3PC() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("D");
		data1.setTransactionCode("21");
		data1.setPosEnv("R");
		data1.setDesc1("ANZ BPAY");
		data1.setAmount(new BigDecimal(-105.6));
		data1.setOriginalCurrencyAmount("sdfgh");
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase3PC() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		//data1.setTransactionType("D");
		data1.setTransactionCode("28");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee("");
		data1.setPosEnv("");
		data1.setTransactionType("");
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase3PCfortxncat() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		//data1.setTransactionType("D");
		data1.setTransactionCode("28");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee("");
		data1.setPosEnv("");
		data1.setTransactionType("");
		data1.setTransactionCategory("cat");
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase3PCfortxncode61() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		//data1.setTransactionType("D");
		data1.setTransactionCode("61");
		//data1.setPosEnv("R");
		data1.setDesc1(null);
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee("");
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase3PCfortxncode63() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		//data1.setTransactionType("D");
		data1.setTransactionCode("63");
		//data1.setPosEnv("R");
		data1.setDesc1("ANZ BPAY");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee("");
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase3PCfortxncode123() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		//data1.setTransactionType("D");
		data1.setTransactionCode("123");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee("");
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("PCB", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsFornullsortingstatus() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setTransactionCode("21");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase1formernamenull() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(1);
		//data1.setTransactionType("D");
		data1.setTransactionCode("21");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setMerchantName(null);
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		try {
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
		}catch(Exception e) {
			
		}
	}
	@Test
	public void testGetEligibleCalsForCase1() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(1);
		//data1.setTransactionType("D");
		data1.setTransactionCode("21");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setMerchantName("ERTAUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase1mernamewithAU() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(1);
		//data1.setTransactionType("D");
		data1.setTransactionCode("21");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setMerchantName("SDFGAU");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase1mernamewithPU() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(1);
		//data1.setTransactionType("D");
		data1.setTransactionCode("21");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setMerchantName("PU");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase1Mercnamewithdiff() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(1);
		//data1.setTransactionType("D");
		data1.setTransactionCode("21");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setMerchantName("SDFGHPU");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	
	@Test
	public void testGetEligibleCalsForcase1() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(1);
		//data1.setTransactionType("D");
		data1.setTransactionCode("21");
		//data1.setPosEnv("R");
		data1.setDesc1("PLUMPTON");
		data1.setMerchantName("PAYPALA");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase2() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("2");
		data1.setTransactionCode("21");
		//data1.setPosEnv("R");
		data1.setDesc2("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligiblecalsForCase2() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(2);
		data1.setTransactionType("06");
		data1.setTransactionCode("09");
		data1.setAmount(new BigDecimal(-105.6));
		data1.setBpayCal("PLUMTON");
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligiblecalsForCase3fortxncodenull() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("6");
		data1.setTransactionCode(null);
		data1.setTransactionCategory(null);
		data1.setTxnEnrichmentID("skaf");
		//data1.setPosEnv("R");
		data1.setDesc4("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligiblecalsForCase3() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("6");
		data1.setTransactionCode("01041");
		data1.setTransactionCategory(null);
		data1.setTxnEnrichmentID("skaf");
		//data1.setPosEnv("R");
		data1.setDesc4("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsForCase3() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("6");
		data1.setTransactionCode("05267");
		data1.setTransactionCategory(null);
		data1.setTxnEnrichmentID("skaf");
		//data1.setPosEnv("R");
		data1.setDesc4("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsforCase3POSfortxncategaory() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("6");
		data1.setTransactionCode("05437");
		data1.setTransactionCategory("sdf");
		data1.setTxnEnrichmentID("skaf");
		//data1.setPosEnv("R");
		data1.setDesc4("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data1.setDesc1("O/S FEE");
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		try {
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
		}catch(Exception e) {
			
		}
	}
	@Test
	public void testGetEligibleCalsforCase3POS() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("6");
		data1.setTransactionCode("05437");
		data1.setTransactionCategory(null);
		data1.setTxnEnrichmentID("skaf");
		//data1.setPosEnv("R");
		data1.setDesc4("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsforCase3forBPAY() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("6");
		data1.setTransactionCode("05381");
		data1.setTransactionCategory(null);
		data1.setTxnEnrichmentID("skaf");
		//data1.setPosEnv("R");
		data1.setDesc4("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsforCase3forDrectDebit() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("6");
		data1.setTransactionCode("05");
		data1.setObTrnType("DIRECT_DEBIT");
		data1.setTransactionCategory(null);
		data1.setTxnEnrichmentID("skaf");
		data1.setStatementDetails("Supriya");
		//data1.setPosEnv("R");
		data1.setDesc4("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	
	@Test
	public void testGetEligibleCalsforCase3forDrectDebitstmtdeailsgreaterthan20() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("6");
		data1.setTransactionCode("05");
		data1.setObTrnType("DIRECT_DEBIT");
		data1.setTransactionCategory(null);
		data1.setTxnEnrichmentID("skaf");
		data1.setStatementDetails("qwertyuiopasdfghjklzxcvbnm");
		//data1.setPosEnv("R");
		data1.setDesc4("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
	}
	@Test
	public void testGetEligibleCalsforCase3() {
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("6");
		data1.setTransactionCode("05437");
		data1.setTransactionCategory("BPAY");
		//data1.setPosEnv("R");
		data1.setDesc4("PLUMPTON O/S FEE");
		data1.setDesc1("PLUMPTON");
		data1.setDesc2("PLUMPTON");
		data1.setDesc3("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		try {
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
		}catch(Exception e) {
			
		}
	}

	@Test
	public void testGetDirectDebitEligibilityForR(){
		TransactionsData data=new TransactionsData();
		data.setPosEnv("R");
		data.setTransactionType("dfghj");
		enrichTxnService.getDirectDebitEligibility(data);
	}
	@Test
	public void testGetDirectDebitEligibilityForI(){
		TransactionsData data=new TransactionsData();
		data.setPosEnv("I");
		data.setTransactionType("dfghj");
		enrichTxnService.getDirectDebitEligibility(data);
	}
	@Test
	public void testGetDirectDebitEligibilityForP(){
		TransactionsData data=new TransactionsData();
		data.setPosEnv("P");
		data.setTransactionType("dfghj");
		enrichTxnService.getDirectDebitEligibility(data);
	}
	
	@Test
	public void testPriorday() {
		ReflectionTestUtils.setField(enrichTxnService, "txnCategoryList", Arrays.asList("BPAY"));
		List<TransactionsData> data=new ArrayList<TransactionsData>();
		TransactionsData data1=new TransactionsData();
		data1.setSortingTransStatus(3);
		data1.setTransactionType("6");
		data1.setTransactionCode("05437");
		data1.setTransactionCategory("BPAY");
		data1.setTxnEnrichmentID("skaf");
		//data1.setPosEnv("R");
		data1.setDesc4("PLUMPTON O/S FEE");
		data1.setDesc1("PLUMPTON");
		data1.setDesc2("PLUMPTON");
		data1.setDesc3("PLUMPTON");
		data1.setMerchantName("AUS");
		data1.setOriginalCurrencyAmount(null);
		data1.setConversionFee(null);
		data1.setAmount(new BigDecimal(-105.6));
		data.add(data1);
		Map<String, String> eligiblecals = new HashMap<String, String>();
		eligiblecals.put("PLUMTON", "PLUMTON");
		try {
		enrichTxnService.getEligibleCals("DDA", data, eligiblecals);
		}catch(Exception e) {
			
		}
		
	}
	
}

	
