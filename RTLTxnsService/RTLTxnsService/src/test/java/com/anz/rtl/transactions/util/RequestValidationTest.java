package com.anz.rtl.transactions.util;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anz.rtl.transactions.request.TransactionAccountDetailRequest;
import com.anz.rtl.transactions.request.TransactionRequestParam;
import com.anz.rtl.transactions.request.TransactionsRequest;

@RunWith(MockitoJUnitRunner.class)
public class RequestValidationTest {

	private static final Logger LOG = LoggerFactory.getLogger(RequestValidationTest.class);
	Calendar calendar = Calendar.getInstance();

	@InjectMocks
	TransactionRequestParam requestParam;

	@InjectMocks
	RequestValidation requestValidation;

	@Test
	public void validateTransTypeTest() {
		TransactionsRequest transactionsRequest = new TransactionsRequest();
		TransactionAccountDetailRequest req = new TransactionAccountDetailRequest();
		req.setProductType("SU");
		transactionsRequest.setAccountInfo(req);
		try {
			RequestValidation.validateProdType(transactionsRequest);
		} catch (final ValidationException e) {
			final String msg = "Not a valid product type ";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateProdType() {
		requestParam.setCreditDebitFlag("T");

		try {
			RequestValidation.validateTransType(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid value for transaction-type.";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateTransTypeTestWhenItIsNull() {
		requestParam.setCreditDebitFlag("");

		try {
			RequestValidation.validateTransType(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid value for transaction-type.";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateTransTypeTestWhenItIsEmpty() {
		// requestParam.setCreditDebitFlag("");

		try {
			RequestValidation.validateTransType(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid value for transaction-type.";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateTransTypeTestWhenItIsC() {
		requestParam.setCreditDebitFlag("C");

		try {
			RequestValidation.validateTransType(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid value for transaction-type.";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateTransTypeTestWhenItIsD() {
		requestParam.setCreditDebitFlag("D");

		try {
			RequestValidation.validateTransType(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid value for transaction-type.";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateChequeNumTestWhenHighChequeNoisZero() {
		requestParam.setLowChequeNum(20);
		requestParam.setHighChequeNum(0);

		try {
			RequestValidation.validateChequeNum(requestParam);
		} catch (final ValidationException e) {
			final String msg = "High cheque number is not provided ";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateChequeNumTestWhenLowAndHighChequeNoisNotZero() {
		requestParam.setLowChequeNum(20);
		requestParam.setHighChequeNum(30);

		try {
			RequestValidation.validateChequeNum(requestParam);
		} catch (final ValidationException e) {
			final String msg = "High cheque number is not provided ";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateChequeNumTestWhenLowChequeNoisZero() {
		requestParam.setLowChequeNum(0);
		requestParam.setHighChequeNum(20);

		try {
			RequestValidation.validateChequeNum(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Low cheque number is not provided ";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateChequeNumTestWhenLowChequeNoisZeroAndHighChequeIsNotZero() {
		requestParam.setLowChequeNum(0);
		requestParam.setHighChequeNum(20);

		try {
			RequestValidation.validateChequeNum(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Low cheque number is not provided ";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateMaxRecordTestWhenGreaterThan4000() {
		requestParam.setRecordLimit(4001);

		try {
			RequestValidation.validateMaxRecord(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Max record size is invalid";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateMaxRecordTestWhenLessThan4000() {
		requestParam.setRecordLimit(40);

		try {
			RequestValidation.validateMaxRecord(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Max record size is invalid";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateDescSearchTestWhenDescIsNotNullAndDescStringIsNull() {
		requestParam.setDescOperator("contains");
		try {
			RequestValidation.validateDescSearch(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid value passed for desc-operator";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateDescSearchTestWhenDescSearchLengthIsLessThan3() {
		requestParam.setDescOperator("Contains");
		requestParam.setDescSearch("AS");
		try {
			RequestValidation.validateDescSearch(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid size for desc search";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateDescSearchTestWhenDescSearchLengthIsMoreThan120() {
		requestParam.setDescOperator("Contains");
		requestParam.setDescSearch(
				"ASfkuhfhfiahfiuhaiuhdsihdfhhfhsahiusafhiusahfsahsahfsahfiuhsafiuhsafhasdfhdsahfiusahdfiuhdsafhdsahfisahfihdsauyiuyiuyuyiu");
		try {
			RequestValidation.validateDescSearch(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid size for desc search";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateDescSearchTestWhenDescSearchLengthIsWithinLimit() {
		requestParam.setDescOperator("contains");
		requestParam.setDescSearch("Payment");
		try {
			RequestValidation.validateDescSearch(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid value passed for desc-operator";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void testValidateRequest(){
		TransactionsRequest request =new TransactionsRequest();
		TransactionAccountDetailRequest tranRequest=new TransactionAccountDetailRequest();
		tranRequest.setAccountId("22344");
		tranRequest.setProductType("PC");
		request.setAccountInfo(tranRequest);
		RequestValidation.validateRequest(request);
	}


	@Test
	public void testCalculateTwoYearBeforeDate(){
		RequestValidation.calculateTwoYearBeforeDate();
	}
	@Test
	public void validateDescSearchTestWhenDescOperatorIsEmpty() {
		requestParam.setDescOperator("");
		requestParam.setDescSearch("Payment");
		try {
			RequestValidation.validateDescSearch(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Desc-operator value is not provided";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateDescSearchTestWhenDescOperatorIsNull() {
		requestParam.setDescOperator("");
		requestParam.setDescSearch("Payment");
		try {
			RequestValidation.validateDescSearch(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Desc-operator value is not provided";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateDescSearchTestWhenDescSearchIsNull() {
		requestParam.setDescOperator("Contains");
		requestParam.setDescSearch(null);
		try {
			RequestValidation.validateDescSearch(requestParam);
		} catch (final ValidationException e) {
			final String msg = "desc-search cannot be null if you are passing desc-operator";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void ValidateDateTestWhenStartAndEndDateBothAreNotProvided() {
		requestParam.setStartDate(null);
		requestParam.setEndTime(null);

		RequestValidation.validateDate(requestParam);

	}

	@Test
	public void ValidateDateTestWhenStartAndEndDateBothAreProvided() {
		requestParam.setStartDate(new Date());
		requestParam.setEndDate(new Date());

		RequestValidation.validateDate(requestParam);

	}

	@Test(expected = ValidationException.class)
	public void ValidateDateTestWhenendDateBeforeTodaysDate() {
		requestParam.setStartDate(new Date());
		LocalDate ld=LocalDate.now().minusDays(1);
		Date d=Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
		requestParam.setEndDate(d);

		RequestValidation.validateDate(requestParam);

	}


	/*
	 * @Test public void ValidateDateTestWhenStartDateIsNull() {
	 * requestParam.setStartDate(null); requestParam.setEndDate(java.sql.Date
	 * .valueOf((new
	 * Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).minusDays(
	 * 150)));
	 * 
	 * try { RequestValidation.validateDate(requestParam); } catch (final
	 * ValidationException e) { final String msg =
	 * "Start date is bigger than end date"; assertEquals(msg, e.getMessage()); }
	 * 
	 * }
	 */
	@Ignore
	@Test
	public void ValidateDateTestWhenStartDateIsNullAndEndDateIsOlderThanTwoYears() {
		requestParam.setStartDate(null);
		requestParam.setEndDate(java.sql.Date
				.valueOf((new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).minusDays(800)));

		try {
			RequestValidation.validateDate(requestParam);
		} catch (final ValidationException e) {
			final String msg = "End date is older than two years date";
			assertEquals(msg, e.getMessage());
		}

	}

	@Test
	public void ValidateDateTestWhenStartDateIsNotNullAndEndDateIsNUll() {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date todaysDate = calendar.getTime();
		System.out.println(todaysDate);
		requestParam.setStartDate(todaysDate);
		requestParam.setEndDate(null);

		RequestValidation.validateDate(requestParam);
	}

	@Test
	public void ValidateDateTestWhenStartDateIsNotNullAndEndDateIsNull() {
		Date date = stringToDate("2018-11-01", "yyyy-MM-dd");

		requestParam.setStartDate(date);
		requestParam.setEndDate(null);
		RequestValidation.validateDate(requestParam);
	}

	@Test
	public void ValidateDateTestWhenStartDateIsNotNullAndEndDateIsNull1() {
		Date date = stringToDate("2019-04-01", "yyyy-MM-dd");

		requestParam.setStartDate(date);
		requestParam.setEndDate(null);
		RequestValidation.validateDate(requestParam);
	}

	@Test
	public void ValidateDateTestWhenStartDateIsNotNullAndEndDateIsNullAndStDtIsOlderThanTwoYears() {
		Date date = stringToDate("2016-11-01", "yyyy-MM-dd");

		requestParam.setStartDate(date);
		requestParam.setEndDate(null);
		RequestValidation.validateDate(requestParam);
	}

	@Test
	public void ValidateDateTestWhenStartDateAndEndDateIsNotSameAndStDtAndEndDateIsAfterTodaysDate() {
		Date date = stringToDate("2020-05-25", "yyyy-MM-dd");
		Date date1 = stringToDate("2020-06-25", "yyyy-MM-dd");
		requestParam.setStartDate(date);
		requestParam.setEndDate(date1);
		try {
			RequestValidation.validateDate(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid Date range";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void ValidateDateTestWhenStartDateAndEndDateIsSameAndStDtIsOlderThanTwoYears() {
		Date date = stringToDate("2018-02-25", "yyyy-MM-dd");
		Date date1 = stringToDate("2018-02-25", "yyyy-MM-dd");
		requestParam.setStartDate(date);
		requestParam.setEndDate(date1);
		try {
			RequestValidation.validateDate(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid Date range";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void ValidateDateTestWhenStartDateIsNotNullAndEndDateIsNullAndStDtIsEqualsTodaysDt() {
		Date date = stringToDate("2019-05-02", "yyyy-MM-dd");

		requestParam.setStartDate(date);
		requestParam.setEndDate(null);
		RequestValidation.validateDate(requestParam);
	}

	@Test
	public void ValidateDateTestWhenStartDateIsNotNullAndEndDateIsNullAndStDtIsAfterTodaysDt() {
		Date date = stringToDate("2020-05-02", "yyyy-MM-dd");

		requestParam.setStartDate(date);
		requestParam.setEndDate(null);
		try {
			RequestValidation.validateDate(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Current date is older than start date";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void ValidateDateTestWhenStartDateAndEndDateIsNotNull() {
		Date startDate = stringToDate("2018-05-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-01-02", "yyyy-MM-dd");

		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		try {
			RequestValidation.validateDate(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Start date is bigger than current date";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void ValidateDateTestWhenStartDateIsNullAndEndDateIsNotNull() {
		// Date startDate = stringToDate("2018-05-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2030-01-02", "yyyy-MM-dd");

		requestParam.setStartDate(null);
		requestParam.setEndDate(endDate);
		try {
			RequestValidation.validateDate(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Start date is bigger than current date";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void ValidateDateTestWhenStartDateIsNullAndEndDateTodaysDate() {
		// Date startDate = stringToDate("2018-05-02", "yyyy-MM-dd");
		Date endDate = stringToDate(LocalDate.now().toString(), "yyyy-MM-dd");

		requestParam.setStartDate(null);
		requestParam.setEndDate(endDate);
		try {
			RequestValidation.validateDate(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Start date is bigger than current date";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void ValidateDateTestWhenStartDateAndEndDateBothAreBeforeTwoYears() {
		Date startDate = stringToDate("2016-05-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2017-01-02", "yyyy-MM-dd");

		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		try {
			RequestValidation.validateDate(requestParam);
		} catch (final ValidationException e) {
			final String msg = "Invalid Date range";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void ValidateDateTestWhenStartDateIsBeforeTwoYears() {
		Date startDate = stringToDate("2016-05-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2018-05-02", "yyyy-MM-dd");

		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		RequestValidation.validateDate(requestParam);
	}

	@Test
	public void ValidateDateTestWhenStartDateIsBeforeTwoYears1() {
		Date startDate = stringToDate("2016-05-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-05-02", "yyyy-MM-dd");

		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		RequestValidation.validateDate(requestParam);
	}

	@Test
	public void ValidateDateTestWhenStartDateIsBeforeTwoYears2() {
		Date startDate = stringToDate("2018-05-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-05-02", "yyyy-MM-dd");

		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		RequestValidation.validateDate(requestParam);
	}

	@Test
	public void ValidateDateTestWhenStartDateIsBeforeTwoYears3() {
		Date startDate = stringToDate("2018-05-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2018-05-02", "yyyy-MM-dd");

		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		RequestValidation.validateDate(requestParam);
	}

	@Test
	public void validateDateWhenStartDateIsAfterEndDate() {
		Date startDate = stringToDate("2019-05-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2018-05-02", "yyyy-MM-dd");
		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		try {
			RequestValidation.validateDate(requestParam);

		} catch (final ValidationException e) {
			final String msg = "End date is older than start date";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateDateWhenStartDateAndEndDateBothAreInFuture() {
		Date startDate = stringToDate("2019-7-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-09-02", "yyyy-MM-dd");
		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		try {
			RequestValidation.validateDate(requestParam);

		} catch (final ValidationException e) {
			final String msg = "Invalid Date range";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateDateWhenStartDateIsAfterEndDates() {
		Date startDate = stringToDate("2019-10-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-09-02", "yyyy-MM-dd");
		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		try {
			RequestValidation.validateDate(requestParam);

		} catch (final ValidationException e) {
			final String msg = "End date is older than start date";
			assertEquals(msg, e.getMessage());
		}
	}

	/*
	 * @Ign public void validateTimeTest() { String field = "Start Time"; String
	 * value = "05:10:00"; try { RequestValidation.validateTime(field, value);
	 * 
	 * } catch (final ValidationException e) { final String msg =
	 * "Unable to parse Start Time"; assertEquals(msg, e.getMessage()); } }
	 */
	@Test
	public void validateTime() {
		Date startDate = stringToDate("2020-03-04", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-03-04", "yyyy-MM-dd");
		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		try {
			RequestValidation.validateTime(requestParam);// validateTime(field, value);

		} catch (final ValidationException e) {
			final String msg = "Start date time is greater than End date time";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateAmountTest() {
		BigDecimal minAmount = new BigDecimal(1000);
		BigDecimal maxAmount = new BigDecimal(2000);
		try {
			RequestValidation.validateAmount(minAmount, maxAmount);

		} catch (final ValidationException e) {
			final String msg = "Min amount is bigger than max amount";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateAmountTestFail() {
		BigDecimal minAmount = new BigDecimal(3000);
		BigDecimal maxAmount = new BigDecimal(2000);
		try {
			RequestValidation.validateAmount(minAmount, maxAmount);

		} catch (final ValidationException e) {
			final String msg = "Min amount is greater than max amount";
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void validateCheuqeNoTest() {
		int lowChequeNo = 234;
		int highChequeNo = 1234;
		try {
			RequestValidation.validateCheuqeNo(lowChequeNo, highChequeNo, "");

		} catch (final ValidationException e) {
			final String msg = "Low cheque no is bigger than high cheque no";
			assertEquals(msg, e.getMessage());
		}

	}

	@Test
	public void validateCheuqeWithProductType() {
		int lowChequeNo = 1234;
		int highChequeNo = 1234;
		String productType = "PC";
		try {
			RequestValidation.validateCheuqeNo(lowChequeNo, highChequeNo, productType);

		} catch (final ValidationException e) {
			final String msg = "Cheque filter is not supported for V+ accounts";
			assertEquals(msg, e.getMessage());
		}

	}

	@Test
	public void validateCheuqeNoTestFail() {
		int lowChequeNo = 123234;
		int highChequeNo = 1234;
		try {
			RequestValidation.validateCheuqeNo(lowChequeNo, highChequeNo, "");

		} catch (final ValidationException e) {
			final String msg = "Low cheque no is greater than high cheque no";
			assertEquals(msg, e.getMessage());
		}

	}

	@Test
	public void ValidateRequest() {
		Date startDate = stringToDate("2018-05-02", "yyyy-MM-dd");
		Date endDate = stringToDate("2019-02-02", "yyyy-MM-dd");
		requestParam.setMinAmount(new BigDecimal(100));
		requestParam.setMaxAmount(new BigDecimal(500));
		requestParam.setLowChequeNum(100);
		requestParam.setHighChequeNum(300);
		requestParam.setStartDate(startDate);
		requestParam.setEndDate(endDate);
		requestParam.setStartTime(LocalTime.of(1, 20));
		requestParam.setEndTime(LocalTime.of(4, 20));
		requestParam.setDescOperator("Contains");
		requestParam.setDescSearch("Payments");
		requestParam.setRecordLimit(100);
		requestParam.setLowChequeNum(100);
		requestParam.setHighChequeNum(200);
		requestParam.setCreditDebitFlag("C");
		RequestValidation.ValidateRequest(requestParam, "", "Y");
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
	@Ignore
	public void testCalculateStartDateEndDateMinus90Days() {
		requestParam.setCreditDebitFlag("T");
		requestParam.setBranchId(624);
		Date endDate = stringToDate("2018-05-02", "yyyy-MM-dd");
		Date twoYearsBfDate = stringToDate("2019-02-02", "yyyy-MM-dd");
		//requestValidation.calculateStartDateEndDateMinus90Days(requestParam, endDate, twoYearsBfDate);
		
	}

	@Test
	public void testSetEndDateToStartDatePlus90Days() {
		requestParam.setCreditDebitFlag("T");
		requestParam.setBranchId(624);
		Date startDate = stringToDate("2020-05-02", "yyyy-MM-dd");
		Date todaysDate = stringToDate("2019-02-02", "yyyy-MM-dd");
		requestValidation.setEndDateToStartDatePlus90Days(requestParam, startDate, todaysDate);
		
	}
}
