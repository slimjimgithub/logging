package com.anz.rtl.transactions.controller;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anz.rtl.transactions.constants.TransactionConstants;
import com.anz.rtl.transactions.dao.RedisTransactionRepository;
import com.anz.rtl.transactions.request.TransactionRequestParam;
import com.anz.rtl.transactions.request.TransactionsRequest;
import com.anz.rtl.transactions.response.Error;
import com.anz.rtl.transactions.response.TransactionsResponse;
import com.anz.rtl.transactions.service.RetailTransactionService;
import com.anz.rtl.transactions.util.TransactionMdcLogger;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * RT Transactions REST Service Controller
 * 
 * @author anz
 */
@RestController
@Api(value = "RetailTransactionsController", tags = "retailtransactions")
@Validated
public class RetailTransactionsController {

	private static final Logger LOG = LoggerFactory.getLogger(RetailTransactionsController.class);
	
	@Value("${enable.merchantInfo.flag}")
	private boolean enabledmerchantInfo;

	@Autowired
	private RetailTransactionService retailTxnService;

	@Autowired
	RedisTransactionRepository redisTransactionRepository;
	/*
	 * @Autowired private RedisTransactionRepository redisRepo;
	 */
	public static final String DATE_FORMAT = "yyyy-MM-dd";

	// @GetMapping(value = "/accounts/v1/transactions", produces =
	// MediaType.APPLICATION_JSON_UTF8_VALUE)
	@RequestMapping(value = "/accounts/transactions", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Obtain transactions for a specific account.", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, notes = "Provides a list of transactions for the specified account. Includes support for filtering and pagination of the transaction data.", tags = "retailtransactions")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Query completed successfully.", response = TransactionsResponse.class),
			@ApiResponse(code = 400, message = "Request has malformed, missing or non-compliant JSON body or URL parameters.", response = Error.class),
			@ApiResponse(code = 422, message = "The request was well formed but was unable to be processed due to business logic specific to the request.", response = Error.class),
			@ApiResponse(code = 500, message = "Something went wrong on the API gateway or microservice.", response = Error.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "X-Orig-App", value = "Name of the application which initiates the transaction", required = true, example = "THA", dataTypeClass = String.class),
			@ApiImplicitParam(name = "X-Request-Id", value = "Request Identifier in UIID format", required = true, example = "db49cd3e-11bc-4e85-a63c-b1e680e1d94a", dataTypeClass = String.class),
			@ApiImplicitParam(name = "X-Effective-Date", value = "This field contains the current date", required = true, example = "2013-09-29", dataType = "Date"),
			@ApiImplicitParam(name = "X-Init-DateTime", value = "Request initiated date and time in country of origin", required = true, example = "2013-09-29T18:46:19Z", dataType = "Date"),
			@ApiImplicitParam(name = "X-Operator-Id", value = "Operator identifier that initiated the transaction", required = true, example = "TXTBANKG", dataTypeClass = String.class),
			@ApiImplicitParam(name = "X-Branch-Id", value = "Branch number where the transaction is initiated", required = true, example = "003026", dataType = "int"),
			@ApiImplicitParam(name = "X-Terminal-Id", value = "Workstation ID where the transaction is initiated, For example Windows Workstation ID, IP Address", required = false, example = "192.169.201.203", dataTypeClass = String.class),
			@ApiImplicitParam(name = "X-Session-TimeOut", value = "Session time out value in minutes."
					+ "\n If X-Session-TimeOut is greater than 30 mintues then default Session TimeOut of 30 Minutes will be used.", required = true, example = "30", dataType = "int"),
			@ApiImplicitParam(name = "Accept", value = "Version number.", required = true, example = "application/json;version=v1", dataTypeClass = String.class),
			@ApiImplicitParam(name = "intraday", value = "Filter to retrieve Current day transactions", required = false, example = "true", defaultValue = "true", dataType = "boolean"),
			@ApiImplicitParam(name = "outstanding", value = "Filter to retrieve Outstanding transaction", required = false, example = "false", defaultValue = "true", dataType = "boolean"),
			@ApiImplicitParam(name = "priorday", value = "Filter to retrieve Priorday transaction", required = false, example = "true", defaultValue = "true", dataType = "boolean"),
			@ApiImplicitParam(name = "start-date", value = "From date of transactions requested.Date Format: YYYY-MM-DD", required = false, example = "2019-01-01", dataType = "string"),
			@ApiImplicitParam(name = "end-date", value = "To date of transactions requested.Date Format:YYYY-MM-DD", required = false, example = "2019-02-28", dataType = "string"),
			@ApiImplicitParam(name = "transaction-type", value = "Filter to retrieve credit and debit transactions"
					+ "\n Valid values : " + "\n C - To filter Credit Transactions"
					+ "\n D - To filter Debit Transactions"
					+ "\n Do not send this attribute if the filter should not be enabled", required = false, example = "D", dataTypeClass = String.class),
			@ApiImplicitParam(name = "low-cheque-num", value = "This field is populated with start Cheque Serial Number."
					+ "Both Start and End Cheque Serial Number need to be populated with valid cheque number to filter the transactions based on Cheque Serial Number range"
					+ "\n Valid Cheque number - Cheque Number will be of 6 digits and greater than zeroes ", required = false, example = "12345", dataType = "int"),
			@ApiImplicitParam(name = "high-cheque-num", value = "This field is populated with end Cheque Serial Number."
					+ "Both Start and End Cheque Serial Number need to be populated with valid cheque number to filter the transactions based on Cheque Serial Number range"
					+ "\n Valid Cheque number - Cheque Number will be of 6 digits and greater than zeroes ", required = false, example = "25677", dataType = "int"),
			@ApiImplicitParam(name = "min-amount", value = "This field represents the minimum amount to filter transactions. Filter conditions as follows:"
					+ "\n1. Populate only Minimum Amount to find transactions with Amount greater than or equal to this amount."
					+ "\n2. Populate the same amount in both Min and Max Amount to filter transactions with amount equal to this amount"
					+ "\n3. Populate both Min and Max amount to filter transactions based on the range"
					+ "\n4. Populate only Max Amount to find transactions with amount less than or equal to this amount", required = false, example = "2000", dataType = "BigDecimal"),
			@ApiImplicitParam(name = "max-amount", value = "This field represents the maximum amount to filter transactions. Filter conditions as follows:"
					+ "\n1. Populate only Minimum Amount to find transactions with Amount greater than or equal to this amount."
					+ "\n2. Populate the same amount in both Min and Max Amount to filter transactions with amount equal to this amount"
					+ "\n3. Populate both Min and Max amount to filter transactions based on the range"
					+ "\n4. Populate only Max Amount to find transactions with amount less than or equal to this amount", required = false, example = "10000", dataType = "BigDecimal"),
			@ApiImplicitParam(name = "start-time", value = "This field represents the Start Time to filter only Current day and Outstanding transactions."
					+ "\nTime format - HH:MM:SS " + "(Hours -24 Hr Format)"
					+ "\nBoth Start Time and End Time need to be populated to filter the transactions based on the time range"
					+ "\nFor Current day and Outstanding transactions, Start Date + Start Time and End Date + End Time will be compared against OriginDateTime"
					+ "\nOriginDateTime >= StartDate+StartTime and <= EndDate+EndTime"
					+ "\nIf only End time is sent by the channel, Start Time of 00:00:00 would be used", required = false, example = "01:22:55", dataType = "string"),
			@ApiImplicitParam(name = "end-time", value = "This field represents the End Time to filter only Current day and Outstanding transactions."
					+ "\nTime format - HH:MM:SS " + "(Hours -24 Hr Format)"
					+ "\nBoth Start Time and End Time need to be populated to filter the transactions based on the time range"
					+ "\nFor Current day and Outstanding transactions, Start Date + Start Time and End Date + End Time will be compared against OriginDateTime"
					+ "\nOriginDateTime >= StartDate+StartTime and <= EndDate+EndTime"
					+ "\nIf only End time is sent by the channel, Start Time of 00:00:00 would be used", required = false, example = "05:00:00", dataType = "string"),
			@ApiImplicitParam(name = "desc-operator", value = "Operator Values associated with Description Search."
					+ "\nPossible Values: " + "\nContains" + "\nStartsWith"
					+ "\nEndsWith", required = false, example = "Contains"),
			@ApiImplicitParam(name = "desc-search", value = "Filter only transactions where this string value is found as a substring of Transaction Narrative field. "
					+ "Format is arbitrary ASCII string. Minimum string size for desc-search is 3.", required = false, example = "payment", dataTypeClass = String.class),
			@ApiImplicitParam(name = "record-limit", value = "Count of the records to be returned in the request "
					+ "\nIf record-limit sent by Channel is greater than 4000 then record-limit will be set to deafulat value of 4000", required = true, example = "200", dataType = "int"),
			@ApiImplicitParam(name = "cursor", value = "Populated on subsequent calls using Cursor value from previous response"
					+ "\nCIS cursor : CIS:2015-06-274 indicates next call to CIS will fetch data from CIS DB"
					+ "\nRedis cursor : RED:aa49cd3e-11bc-4e85-a63c-b1e680e1d94aTHA110530248:10 indicates next call to CIS will fetch data from Redis cache", required = false, example = "CIS:2019-03-1520", dataTypeClass = String.class),
	      @ApiImplicitParam(name = "include-merchant-info", value = "Flag to include additional Merchant Details based on LWC API response."
			+ "\n Valid values : " + "\n true - LWC API will be invoked to include additional Merchant Details \n false - LWC API will not be invoked"
			+ "\n Default Value: false", required = false, example = "false", dataType = "boolean")})

	public ResponseEntity<TransactionsResponse> rtltransactions(
			@RequestHeader(value = "X-Orig-App", required = true) @Size(min = 3, max = 3, message = "{orig.app.name}") @NotBlank(message = "{Orig.app.name.null}") String origApp,
			@RequestHeader(value = "X-Request-Id", required = true) @Size(min = 36, max = 36, message = "{requestid.size}") @NotEmpty(message = "{requestid.empty}") String requestId,
			@RequestHeader(value = "X-Effective-Date", required = true) @NotNull(message = "{effdate.null}") @DateTimeFormat(pattern = "yyyy-MM-dd") Date effDate,
			@RequestHeader(value = "X-Init-DateTime", required = true) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") @NotNull(message = "{initDateTime.null}") Date initDateTime,
			@RequestHeader(value = "X-Operator-Id", required = true) @Size(max = 8, message = "{operatorId.size}") @NotEmpty(message = "{operatorId.null}") String operatorId,
			@RequestHeader(value = "X-Branch-Id", required = true) @Max(value = 999999, message = "Branch Id value cannot be greather than 999999") @NotNull(message = "{branchId.null}") Integer branchId,
			@RequestHeader(value = "X-Terminal-Id", required = false) @Size(max = 16, message = "{terminal.id.size}") String terminalId,
			@RequestHeader(value = "X-Session-TimeOut", required = true) @Positive(message = "{session.timeout.value}") @NotNull(message = "{session.timeout}") Integer sessionTimeOut,
			@RequestHeader(value = "Accept", required = true) @NotBlank(message = "{version.name.null}") String version,
			@RequestParam(value = "intraday", required = false, defaultValue = "true") Boolean intradayFlag,
			@RequestParam(value = "outstanding", required = false, defaultValue = "true") Boolean outstandingFlag,
			@RequestParam(value = "priorday", required = false, defaultValue = "true") Boolean priordayFlag,
			@RequestParam(value = "start-date", required = false) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
			@RequestParam(value = "end-date", required = false) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
			@RequestParam(value = "transaction-type", required = false) String creditDebitFlag,
			@RequestParam(value = "low-cheque-num", required = false, defaultValue = "0") @PositiveOrZero(message = "{low.cheque.no}") Integer lowChequeNum,
			@RequestParam(value = "high-cheque-num", required = false, defaultValue = "0") @PositiveOrZero(message = "{high.cheque.no}") Integer highChequeNum,
			@RequestParam(value = "min-amount", required = false) @PositiveOrZero(message = "Min amount should be a positive value") BigDecimal minAmount,
			@RequestParam(value = "max-amount", required = false) @PositiveOrZero(message = "Max amount should be a positive value") BigDecimal maxAmount,
			@RequestParam(value = "start-time", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
			@RequestParam(value = "end-time", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
			@RequestParam(value = "desc-operator", required = false) String descOperator,
			@RequestParam(value = "desc-search", required = false) String descSearch,
			@RequestParam(value = "record-limit", required = true) @Positive(message = "Record Limit should be a positive value and greater than zero") @NotNull(message = "Record Limit is mandatory") Integer recordLimit,
			@RequestParam(value = "cursor", required = false) @Size(max = 200, message = "{cursor.size}") String cursor,
			@RequestParam(value = "include-merchant-info", required = false, defaultValue = "false") Boolean includeMerchantInfo,
			@Valid @RequestBody TransactionsRequest request) {

		MDC.clear();
		MDC.put(TransactionConstants.REQUEST_ID, requestId);
		long startTs = System.currentTimeMillis();
		LOG.debug("Request has reached to gateway "+"Request Id is : "+requestId);

		TransactionRequestParam param = MapRequestParam(origApp, requestId, effDate, initDateTime, operatorId, branchId, terminalId,
				intradayFlag, priordayFlag, outstandingFlag, creditDebitFlag, startDate, endDate, lowChequeNum,
				highChequeNum, minAmount, maxAmount, startTime, endTime, descOperator, descSearch, recordLimit,
				sessionTimeOut, cursor, includeMerchantInfo);
		LOG.debug("GateWay calling to flow decision ");

		ResponseEntity<TransactionsResponse> response=null;
		response = retailTxnService.flowDecisionProcessor(param, request,response);

		long endTs = System.currentTimeMillis();
		long timeTaken = endTs - startTs;
		LOG.info("Total time taken to process the request for the Request Id : " +requestId+ ", Total time taken : "+ timeTaken);
		LOG.debug(" {} | Status : {}, Headers : {}", response.getStatusCode(), response.getHeaders().toString());
		TransactionMdcLogger.writeToMDCLog(requestId, response.getStatusCode().name(), timeTaken, origApp,
				request.getAccountInfo().getProductType(), request.getAccountInfo().getAccountId(),
				response.getBody().getControlRecord().getRecordSent(), cursor,
				response.getBody().getControlRecord().getCursor(), startDate, endDate);
		return new ResponseEntity<TransactionsResponse>(response.getBody(), response.getStatusCode());
	}

	@RequestMapping(value = "v1/accounts/transactions", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Obtain transactions for a specific account.", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, notes = "Provides a list of transactions for the specified account. Includes support for filtering and pagination of the transaction data.", tags = "retailtransactions")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Query completed successfully.", response = TransactionsResponse.class),
			@ApiResponse(code = 400, message = "Request has malformed, missing or non-compliant JSON body or URL parameters.", response = Error.class),
			@ApiResponse(code = 422, message = "The request was well formed but was unable to be processed due to business logic specific to the request.", response = Error.class),
			@ApiResponse(code = 500, message = "Something went wrong on the API gateway or microservice.", response = Error.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "X-Orig-App", value = "Name of the application which initiates the transaction", required = true, example = "THA", dataTypeClass = String.class),
			@ApiImplicitParam(name = "X-Request-Id", value = "Request Identifier in UIID format", required = true, example = "db49cd3e-11bc-4e85-a63c-b1e680e1d94a", dataTypeClass = String.class),
			@ApiImplicitParam(name = "X-Effective-Date", value = "This field contains the current date", required = true, example = "2013-09-29", dataType = "Date"),
			@ApiImplicitParam(name = "X-Init-DateTime", value = "Request initiated date and time in country of origin", required = true, example = "2013-09-29T18:46:19Z", dataType = "Date"),
			@ApiImplicitParam(name = "X-Operator-Id", value = "Operator identifier that initiated the transaction", required = true, example = "TXTBANKG", dataTypeClass = String.class),
			@ApiImplicitParam(name = "X-Branch-Id", value = "Branch number where the transaction is initiated", required = true, example = "003026", dataType = "int"),
			@ApiImplicitParam(name = "X-Terminal-Id", value = "Workstation ID where the transaction is initiated, For example Windows Workstation ID, IP Address", required = false, example = "192.169.201.203", dataTypeClass = String.class),
			@ApiImplicitParam(name = "X-Session-TimeOut", value = "Session time out value in minutes."
					+ "\n If X-Session-TimeOut is greater than 30 mintues then default Session TimeOut of 30 Minutes will be used.", required = true, example = "30", dataType = "int"),
			@ApiImplicitParam(name = "Accept", value = "Version number.", required = true, example = "application/json;version=v1", dataTypeClass = String.class),
			@ApiImplicitParam(name = "intraday", value = "Filter to retrieve Current day transactions", required = false, example = "true", defaultValue = "true", dataType = "boolean"),
			@ApiImplicitParam(name = "outstanding", value = "Filter to retrieve Outstanding transaction", required = false, example = "false", defaultValue = "true", dataType = "boolean"),
			@ApiImplicitParam(name = "priorday", value = "Filter to retrieve Priorday transaction", required = false, example = "true", defaultValue = "true", dataType = "boolean"),
			@ApiImplicitParam(name = "start-date", value = "From date of transactions requested.Date Format: YYYY-MM-DD", required = false, example = "2019-01-01", dataType = "string"),
			@ApiImplicitParam(name = "end-date", value = "To date of transactions requested.Date Format:YYYY-MM-DD", required = false, example = "2019-02-28", dataType = "string"),
			@ApiImplicitParam(name = "transaction-type", value = "Filter to retrieve credit and debit transactions"
					+ "\n Valid values : " + "\n C - To filter Credit Transactions"
					+ "\n D - To filter Debit Transactions"
					+ "\n Do not send this attribute if the filter should not be enabled", required = false, example = "D", dataTypeClass = String.class),
			@ApiImplicitParam(name = "low-cheque-num", value = "This field is populated with start Cheque Serial Number."
					+ "Both Start and End Cheque Serial Number need to be populated with valid cheque number to filter the transactions based on Cheque Serial Number range"
					+ "\n Valid Cheque number - Cheque Number will be of 6 digits and greater than zeroes ", required = false, example = "12345", dataType = "int"),
			@ApiImplicitParam(name = "high-cheque-num", value = "This field is populated with end Cheque Serial Number."
					+ "Both Start and End Cheque Serial Number need to be populated with valid cheque number to filter the transactions based on Cheque Serial Number range"
					+ "\n Valid Cheque number - Cheque Number will be of 6 digits and greater than zeroes ", required = false, example = "25677", dataType = "int"),
			@ApiImplicitParam(name = "min-amount", value = "This field represents the minimum amount to filter transactions. Filter conditions as follows:"
					+ "\n1. Populate only Minimum Amount to find transactions with Amount greater than or equal to this amount."
					+ "\n2. Populate the same amount in both Min and Max Amount to filter transactions with amount equal to this amount"
					+ "\n3. Populate both Min and Max amount to filter transactions based on the range"
					+ "\n4. Populate only Max Amount to find transactions with amount less than or equal to this amount", required = false, example = "2000", dataType = "BigDecimal"),
			@ApiImplicitParam(name = "max-amount", value = "This field represents the maximum amount to filter transactions. Filter conditions as follows:"
					+ "\n1. Populate only Minimum Amount to find transactions with Amount greater than or equal to this amount."
					+ "\n2. Populate the same amount in both Min and Max Amount to filter transactions with amount equal to this amount"
					+ "\n3. Populate both Min and Max amount to filter transactions based on the range"
					+ "\n4. Populate only Max Amount to find transactions with amount less than or equal to this amount", required = false, example = "10000", dataType = "BigDecimal"),
			@ApiImplicitParam(name = "start-time", value = "This field represents the Start Time to filter only Current day and Outstanding transactions."
					+ "\nTime format - HH:MM:SS " + "(Hours -24 Hr Format)"
					+ "\nBoth Start Time and End Time need to be populated to filter the transactions based on the time range"
					+ "\nFor Current day and Outstanding transactions, Start Date + Start Time and End Date + End Time will be compared against OriginDateTime"
					+ "\nOriginDateTime >= StartDate+StartTime and <= EndDate+EndTime"
					+ "\nIf only End time is sent by the channel, Start Time of 00:00:00 would be used", required = false, example = "01:22:55", dataType = "string"),
			@ApiImplicitParam(name = "end-time", value = "This field represents the End Time to filter only Current day and Outstanding transactions."
					+ "\nTime format - HH:MM:SS " + "(Hours -24 Hr Format)"
					+ "\nBoth Start Time and End Time need to be populated to filter the transactions based on the time range"
					+ "\nFor Current day and Outstanding transactions, Start Date + Start Time and End Date + End Time will be compared against OriginDateTime"
					+ "\nOriginDateTime >= StartDate+StartTime and <= EndDate+EndTime"
					+ "\nIf only End time is sent by the channel, Start Time of 00:00:00 would be used", required = false, example = "05:00:00", dataType = "string"),
			@ApiImplicitParam(name = "desc-operator", value = "Operator Values associated with Description Search."
					+ "\nPossible Values: " + "\nContains" + "\nStartsWith"
					+ "\nEndsWith", required = false, example = "Contains"),
			@ApiImplicitParam(name = "desc-search", value = "Filter only transactions where this string value is found as a substring of Transaction Narrative field. "
					+ "Format is arbitrary ASCII string. Minimum string size for desc-search is 3.", required = false, example = "payment", dataTypeClass = String.class),
			@ApiImplicitParam(name = "record-limit", value = "Count of the records to be returned in the request "
					+ "\nIf record-limit sent by Channel is greater than 4000 then record-limit will be set to deafulat value of 4000", required = true, example = "200", dataType = "int"),
			@ApiImplicitParam(name = "cursor", value = "Populated on subsequent calls using Cursor value from previous response"
					+ "\nCIS cursor : CIS:2015-06-274 indicates next call to CIS will fetch data from CIS DB"
					+ "\nRedis cursor : RED:aa49cd3e-11bc-4e85-a63c-b1e680e1d94aTHA110530248:10 indicates next call to CIS will fetch data from Redis cache", required = false, example = "CIS:2019-03-1520", dataTypeClass = String.class),
	      @ApiImplicitParam(name = "include-merchant-info", value = "Flag to include additional Merchant Details based on LWC API response."
			+ "\n Valid values : " + "\n true - LWC API will be invoked to include additional Merchant Details \n false - LWC API will not be invoked"
			+ "\n Default Value: false", required = false, example = "false", dataType = "boolean")})

	public ResponseEntity<TransactionsResponse> transactions(
			@RequestHeader(value = "X-Orig-App", required = true) @Size(min = 3, max = 3, message = "{orig.app.name}") @NotBlank(message = "{Orig.app.name.null}") String origApp,
			@RequestHeader(value = "X-Request-Id", required = true) @Size(min = 36, max = 36, message = "{requestid.size}") @NotEmpty(message = "{requestid.empty}") String requestId,
			@RequestHeader(value = "X-Effective-Date", required = true) @NotNull(message = "{effdate.null}") @DateTimeFormat(pattern = "yyyy-MM-dd") Date effDate,
			@RequestHeader(value = "X-Init-DateTime", required = true) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") @NotNull(message = "{initDateTime.null}") Date initDateTime,
			@RequestHeader(value = "X-Operator-Id", required = true) @Size(max = 8, message = "{operatorId.size}") @NotEmpty(message = "{operatorId.null}") String operatorId,
			@RequestHeader(value = "X-Branch-Id", required = true) @Max(value = 999999, message = "Branch Id value cannot be greather than 999999") @NotNull(message = "{branchId.null}") Integer branchId,
			@RequestHeader(value = "X-Terminal-Id", required = false) @Size(max = 16, message = "{terminal.id.size}") String terminalId,
			@RequestHeader(value = "X-Session-TimeOut", required = true) @Positive(message = "{session.timeout.value}") @NotNull(message = "{session.timeout}") Integer sessionTimeOut,
			@RequestHeader(value = "Accept", required = true) @NotBlank(message = "{version.name.null}") String version,
			@RequestParam(value = "intraday", required = false, defaultValue = "true") Boolean intradayFlag,
			@RequestParam(value = "outstanding", required = false, defaultValue = "true") Boolean outstandingFlag,
			@RequestParam(value = "priorday", required = false, defaultValue = "true") Boolean priordayFlag,
			@RequestParam(value = "start-date", required = false) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
			@RequestParam(value = "end-date", required = false) @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
			@RequestParam(value = "transaction-type", required = false) String creditDebitFlag,
			@RequestParam(value = "low-cheque-num", required = false, defaultValue = "0") @PositiveOrZero(message = "{low.cheque.no}") Integer lowChequeNum,
			@RequestParam(value = "high-cheque-num", required = false, defaultValue = "0") @PositiveOrZero(message = "{high.cheque.no}") Integer highChequeNum,
			@RequestParam(value = "min-amount", required = false) @PositiveOrZero(message = "Min amount should be a positive value") BigDecimal minAmount,
			@RequestParam(value = "max-amount", required = false) @PositiveOrZero(message = "Max amount should be a positive value") BigDecimal maxAmount,
			@RequestParam(value = "start-time", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
			@RequestParam(value = "end-time", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
			@RequestParam(value = "desc-operator", required = false) String descOperator,
			@RequestParam(value = "desc-search", required = false) String descSearch,
			@RequestParam(value = "record-limit", required = true) @Positive(message = "Record Limit should be a positive value and greater than zero") @NotNull(message = "Record Limit is mandatory") Integer recordLimit,
			@RequestParam(value = "cursor", required = false) @Size(max = 200, message = "{cursor.size}") String cursor,
			@RequestParam(value = "include-merchant-info", required = false, defaultValue = "false") Boolean includeMerchantInfo,
			@Valid @RequestBody TransactionsRequest request) {

		MDC.clear();
		MDC.put(TransactionConstants.REQUEST_ID, requestId);
		long startTs = System.currentTimeMillis();
		LOG.debug("Request has reached to gateway "+"Request Id is : "+requestId);

		TransactionRequestParam param = MapRequestParam(origApp, requestId, effDate, initDateTime, operatorId, branchId, terminalId,
				intradayFlag, priordayFlag, outstandingFlag, creditDebitFlag, startDate, endDate, lowChequeNum,
				highChequeNum, minAmount, maxAmount, startTime, endTime, descOperator, descSearch, recordLimit,
				sessionTimeOut, cursor, includeMerchantInfo);
		LOG.debug("GateWay calling to flow decision ");

		param.setApiVersion("V1.1");
		ResponseEntity<TransactionsResponse> response=null;
		response = retailTxnService.flowDecisionProcessor(param, request,response);

		long endTs = System.currentTimeMillis();
		long timeTaken = endTs - startTs;
		LOG.info("Total time taken to process the request for the Request Id : " +requestId+ ", Total time taken : "+ timeTaken);
		LOG.debug(" {} | Status : {}, Headers : {}", response.getStatusCode(), response.getHeaders().toString());
		TransactionMdcLogger.writeToMDCLog(requestId, response.getStatusCode().name(), timeTaken, origApp,
				request.getAccountInfo().getProductType(), request.getAccountInfo().getAccountId(),
				response.getBody().getControlRecord().getRecordSent(), cursor,
				response.getBody().getControlRecord().getCursor(), startDate, endDate);
		return new ResponseEntity<TransactionsResponse>(response.getBody(), response.getStatusCode());
	}
	/*
	 * @PostMapping(value = "/deleterediskey") public void
	 * deleteRedisKey(@RequestParam(value = "key") Object key) {
	 * System.out.println("Deleting redis key : " + key);
	 * redisTransactionRepository.deleteKey(key); }
	 */

	/*
	 * Request param Mapping
	 */
	public TransactionRequestParam MapRequestParam(String origApp, String requestId, Date effDate, Date initDateTime,
			String operatorId, Integer branchId, String terminalId, Boolean intradayFlag, Boolean priordayFlag,
			Boolean outstandingFlag, String creditDebitFlag, Date startDate, Date endDate, Integer lowChequeNum,
			Integer highChequeNum, BigDecimal minAmount, BigDecimal maxAmount, LocalTime startTime, LocalTime endTime,
			String descOperator, String descSearch, Integer recordLimit, int sessionTimeOut, String cursor, Boolean includeMerchantInfo) {
		TransactionRequestParam param = new TransactionRequestParam();
		param.setBranchId(branchId);
		param.setRequestId(requestId);
		param.setTerminalId(terminalId);
		param.setEffDate(effDate);
		param.setOperatorId(operatorId);
		param.setInitDateTime(initDateTime);
		param.setOrigApp(origApp);
		param.setRecordLimit(recordLimit);
		param.setCreditDebitFlag(creditDebitFlag);
		param.setSessionTimeOut(sessionTimeOut);

		if (priordayFlag) {
			param.setPriordayFlag(1);
		} else {
			param.setPriordayFlag(0);
		}
		if (outstandingFlag) {
			param.setOutstandingFlag(1);
		} else {
			param.setOutstandingFlag(0);
		}

		if (intradayFlag) {
			param.setIntradayFlag(1);
		} else {
			param.setIntradayFlag(0);
		}

		param.setLowChequeNum(lowChequeNum);
		param.setHighChequeNum(highChequeNum);
		if (null == minAmount) {
			param.setMinAmount(null);
		} else {
			param.setMinAmount(minAmount);
		}
		if (null == maxAmount) {
			param.setMaxAmount(null);
		} else {
			param.setMaxAmount(maxAmount);
		}

		param.setStartTime(startTime);
		param.setEndTime(endTime);
		param.setStartDate(startDate);
		param.setEndDate(endDate);

		param.setDescOperator(descOperator);
		param.setDescSearch(descSearch);
		param.setCursor(cursor);
		if (enabledmerchantInfo && includeMerchantInfo) {
			param.setIncludeMerchantInfo(1);
		} else {
			param.setIncludeMerchantInfo(0);
		}
		return param;
	}

}
