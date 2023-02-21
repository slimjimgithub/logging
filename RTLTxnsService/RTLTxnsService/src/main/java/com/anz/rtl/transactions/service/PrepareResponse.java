package com.anz.rtl.transactions.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.anz.rtl.transactions.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.anz.rtl.transactions.constants.TransactionConstants;

@Component
public class PrepareResponse {
	private static final Logger LOG = LoggerFactory.getLogger(PrepareResponse.class);
	
	
	

	public ResponseEntity<TransactionsResponse> prepareTransactionResponse(
			List<ResponseEntity<TransactionsResponse>> responseList) {
		ResponseEntity<TransactionsResponse> response = null;
		Integer statusCode=200;
		if(!CollectionUtils.isEmpty(responseList)){
			LOG.debug("inside prepareResponse , responseList size is: {} ",responseList.size());
			List<TransactionsData> transData = null;
			TransactionsResponse mergeresponse;
			RecordControlResponse ctrlRecord;

			long startTs = System.currentTimeMillis();
			response = new ResponseEntity<>(HttpStatus.OK);
				String priordayCursor = null;
				int size = responseList.size();
				transData = new ArrayList<TransactionsData>();

				for (ResponseEntity<TransactionsResponse> transactionsResponseEntity : responseList) {
					if (206!=transactionsResponseEntity.getBody().getStatus().getStatusCode()) {
						List<TransactionsData> TransDataRes = transactionsResponseEntity.getBody().getTransactions();
						//if (!CollectionUtils.isEmpty(TransDataRes)) {

						if (transactionsResponseEntity.getBody().getControlRecord() != null &&//"POSTED".equals(TransDataRes.get(0).getStatus()) &&
								transactionsResponseEntity.getBody().getControlRecord().getCursor() != null) {
							priordayCursor = transactionsResponseEntity.getBody().getControlRecord().getCursor();
						}
						if (!CollectionUtils.isEmpty(TransDataRes))
							transData.addAll(TransDataRes);
						//}
					}else {
						statusCode=206;
					}
					if (size == 1) {
						mergeresponse = new TransactionsResponse();
						mergeresponse.setAccountInfo(transactionsResponseEntity.getBody().getAccountInfo());
						mergeresponse.setRequestId(transactionsResponseEntity.getBody().getRequestId());
						ZonedDateTime date= LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
						mergeresponse.setResponseDate(date);
						ctrlRecord = new RecordControlResponse();
						if (priordayCursor != null) {
							ctrlRecord.setCursor(priordayCursor);
						}
						if (!CollectionUtils.isEmpty(transData)) {
							ctrlRecord.setRecordSent(transData.size());
						}else {
							ctrlRecord.setRecordSent(0);
						}
						mergeresponse.setControlRecord(ctrlRecord);
						response = new ResponseEntity<TransactionsResponse>(mergeresponse, HttpStatus.OK);

					}
					size--;
				}
				long timeTaken = System.currentTimeMillis() - startTs;
				MDC.put(TransactionConstants.MERGE_TIME, String.valueOf(timeTaken));
				LOG.debug("Response merge has taken time : " +(System.currentTimeMillis() - startTs));
				// Transaction to be sorted by sortedDate parameter.
				if (!CollectionUtils.isEmpty(transData)) {
					sortResponse(transData,response);
					response.getBody().setTransactions(transData);
				}

		}
		if(response.getBody()!=null) {
			
			response.getBody().setStatus(new Status("Success",statusCode));
		}
		return response;
	}

	public ResponseEntity<TransactionsResponse> sortResponse(List<TransactionsData> transData, ResponseEntity<TransactionsResponse> response) {
		Long startTs = System.currentTimeMillis();
		// Compare sorted Date in descending order
		Comparator<TransactionsData> compareBySortedDate = Comparator.comparing(TransactionsData::getSortingDate,
				(o1, o2) -> {
					return o2.compareTo(o1);
				});

		Comparator<TransactionsData> compareBySeq = Comparator.comparing(TransactionsData::getSeqNo, (o1, o2) -> {
			return o2.compareTo(o1);
		});
		// Compare Transaction status in such order (OS(1)->In(2)->BS(3)->PD(4))
		Comparator<TransactionsData> compareStatus = Comparator.comparing(TransactionsData::getSortingTransStatus);

		// Compare by first name and then last name (multiple fields)
		Comparator<TransactionsData> compareByDateAndStatus = compareBySortedDate.thenComparing(compareStatus)
				.thenComparing(compareBySeq);

		Collections.sort(transData, compareByDateAndStatus);
		long timeTaken = System.currentTimeMillis() - startTs;
		MDC.put(TransactionConstants.SORT_TIME, String.valueOf(timeTaken));
		return response;
	}

/**
 * Map enrich response with Transaction Response object
 * @param EnrichTransactionResponse enrichResponse
 * @param response
 * @param apiVersion 
 * @return
 */
	public ResponseEntity<TransactionsResponse> mergeLWCEnrichResponse(ResponseEntity<EnrichTransactionResponse> enrichResponse,ResponseEntity<TransactionsResponse> response, String apiVersion) {
		LOG.debug("LWC response mapped with transactions started ");
		Long startTs = System.currentTimeMillis();
		List <AdditionalMerchantDetails>enrichedCals= enrichResponse.getBody().getMerchantData();
		List<TransactionsData> transactionsDatas = response.getBody().getTransactions();
		for (AdditionalMerchantDetails caldetail : enrichedCals) {
			String cal=caldetail.getCal();
			List<TransactionsData> transactions=filterCalTrans(cal,transactionsDatas);
			if (!CollectionUtils.isEmpty(transactions)) {
				for(TransactionsData trans:transactions) {
					trans.setEnrichmentID(caldetail.getTxnEnrichmentId());
					trans.setAdditionalMerchantInfo(caldetail);

					// Only show apcanbr and biller code in case of V1.1
					if ("V1.1".equalsIgnoreCase(apiVersion)) {
						if (caldetail.getbPayDetails() != null && caldetail.getbPayDetails().getBillerCodeList() != null
								&& caldetail.getbPayDetails().getBillerCodeList().size() == 1) {
							if (trans.getBpayDetails() == null) {
								trans.setBpayDetails(new BPayDetails());
							}
							trans.getBpayDetails()
									.setBillerCode(caldetail.getbPayDetails().getBillerCodeList().get(0).toString());
						}
					}else {
						if(trans.getBpayDetails() != null) {
							trans.getBpayDetails().setApcaNum(null);
						}
					}
					
				}
			}
		}
		LOG.info("LWC response enrich with transactions time taken: {} ",System.currentTimeMillis() - startTs);
		return response;

	}

	/**
	 * Filter all transaction which contains merchentNm
	 * @param merchentNm
	 * @param trans
	 * @return
	 */
	private List<TransactionsData> filterCalTrans(String merchentNm, List<TransactionsData> trans) {
		return trans.stream().filter(x -> (x.getCal() != null && merchentNm != null
				&& x.getCal().trim().equalsIgnoreCase(merchentNm.trim()))).collect(Collectors.toList());
	}

/*public ResponseEntity<EnrichTransactionResponse> getMerchentResponse() {
	ResponseEntity<EnrichTransactionResponse> merchentRes=null;
	EnrichTransactionResponse merResponse=new EnrichTransactionResponse();
	Status status=new Status("Success", 200);
	merResponse.setStatus(status);
	merResponse.setRequestId("123");
	List<AdditionalMerchantDetails> cals = new ArrayList<AdditionalMerchantDetails>();
	AdditionalMerchantDetails cal1 = new AdditionalMerchantDetails();
	cal1.setMerchantId("123");
	cal1.setMerchantPrimaryName("Goro");
	cal1.setState("karnataka");
	cals.add(cal1);
	AdditionalMerchantDetails cal2 = new AdditionalMerchantDetails();
	cal2.setMerchantId("124");
	cal2.setMerchantPrimaryName("Kallo");
	cal2.setState("karnataka");
	cals.add(cal2);
	merResponse.setMerchantData(cals);;
	merchentRes=new ResponseEntity<EnrichTransactionResponse>(merResponse, HttpStatus.OK);
	return merchentRes;
}

	public ResponseEntity<TransactionsResponse> getTransData(){
		TransactionsResponse transresponse =new TransactionsResponse();
		ResponseEntity<TransactionsResponse> response=null;
		transresponse.setRequestId("123");
		Status status=new Status("SUCCESS", 200);
		transresponse.setStatus(status);
		List<TransactionsData> transactionData= new ArrayList<TransactionsData>();
		SimpleDateFormat dateFormat = new	  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT") );
		String cal1="Goro";
		String cal2="Kallo";
		String cal3="Kaith";
		String cal4="Anuja";
		TransactionsData transData1 = new TransactionsData();
		transData1.setCal(cal1); transData1.setTransactionId("t1");
		transData1.setSortingTransStatus(4);
		transData1.setStatus("Priorday");
		transactionData.add(transData1);
		TransactionsData transData2 = new TransactionsData();
		transData2.setCal(cal2);
		transData2.setTransactionId("t1");
		//transData2.setSortingDate(date2);
		transData2.setSortingTransStatus(3);
		transData2.setStatus("Blindspot");
		transactionData.add(transData2);
		TransactionsData transData3 = new TransactionsData();
		transData3.setCal(cal3);
		transData3.setTransactionId("t1");
		//transData3.setSortingDate(date3);
		transData3.setSortingTransStatus(2);
		transData3.setStatus("Intraday");
		transactionData.add(transData3);
		TransactionsData transData4 = new TransactionsData();
		transData4.setCal(cal4);
		transData4.setTransactionId("t1");
		//transData4.setSortingDate(date3);
		transData4.setSortingTransStatus(1);
		transData4.setStatus("Outstansing");
		transactionData.add(transData4);
		transresponse.setTransactions(transactionData);
		response= new ResponseEntity<TransactionsResponse>(transresponse,HttpStatus.OK);

		return response;

	}*/

}
