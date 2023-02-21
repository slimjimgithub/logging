package com.anz.rtl.transactions.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anz.rtl.transactions.request.ProductType;
import com.anz.rtl.transactions.request.TransactionRequestParam;
import com.anz.rtl.transactions.request.TransactionsRequest;

public class RequestValidation {

	private static final Logger LOG = LoggerFactory.getLogger(RequestValidation.class);

	public static void ValidateRequest(TransactionRequestParam requestParam, String productType,
			String enableDateValidation) {

		LOG.info("Request validation started");
		LOG.info("Date Validation config {}", enableDateValidation);
		Long startTs = System.currentTimeMillis();

		BigDecimal minAmount = requestParam.getMinAmount();
		BigDecimal maxAmount = requestParam.getMaxAmount();
		int lowChequeNo = requestParam.getLowChequeNum();
		int highChequeNo = requestParam.getHighChequeNum();

		if (enableDateValidation.equalsIgnoreCase("Y")) {
			LOG.info("enableDateValidation validation started");
			validateDate(requestParam);
		}
		// validateTime("Start Time", requestParam.getStartTime());
		// validateTime("End Time", requestParam.getEndTime());
		validateDescSearch(requestParam);
		validateMaxRecord(requestParam);
		validateChequeNum(requestParam);
		validateTransType(requestParam);

		// validateTime(requestParam);

		if (minAmount != null && maxAmount != null) {
			validateAmount(minAmount, maxAmount);
		}
		if (lowChequeNo != 0 && highChequeNo != 0) {
			validateCheuqeNo(lowChequeNo, highChequeNo, productType);
		}

		validateTime(requestParam);
		LOG.info("Request validation completed. Time taken in request validation : "
				+ (System.currentTimeMillis() - startTs));

	}

	public static void validateProdType(TransactionsRequest request) {
		String prodtyp = request.getAccountInfo().getProductType().toUpperCase();
		boolean isValid = false;
		if (prodtyp.startsWith("PC")) {
			isValid = true;
		} else {
			isValid = ProductType.contains(request.getAccountInfo().getProductType().toUpperCase());
		}
		if (false == isValid) {
			throw new ValidationException("Not a valid product type ");
		}
	}

	public static void validateTime(TransactionRequestParam request) {
		String startDtAndTm = null;
		String EndDtAndTm = null;

		if (request.getStartTime() == null) {
			startDtAndTm = Util.dateToString(request.getStartDate(), "yyyy-MM-dd") + "T" + "00:00:00";
		} else if (0 == request.getStartTime().getSecond()) {
			startDtAndTm = Util.dateToString(request.getStartDate(), "yyyy-MM-dd") + "T"
					+ request.getStartTime().plusNanos(01);
		} else {
			startDtAndTm = Util.dateToString(request.getStartDate(), "yyyy-MM-dd") + "T" + request.getStartTime();
		}

		if (request.getEndTime() == null) {
			EndDtAndTm = Util.dateToString(request.getEndDate(), "yyyy-MM-dd") + "T" + "23:59:59";
		} else if (0 == request.getEndTime().getSecond()) {
			EndDtAndTm = Util.dateToString(request.getEndDate(), "yyyy-MM-dd") + "T"
					+ request.getEndTime().plusNanos(01);
		} else {
			EndDtAndTm = Util.dateToString(request.getEndDate(), "yyyy-MM-dd") + "T" + request.getEndTime();
		}
		try {
			Date startTimeDT = Util.dateParse(startDtAndTm, "yyyy-MM-dd'T'HH:mm:ss");
			Date endTimeDT = Util.dateParse(EndDtAndTm, "yyyy-MM-dd'T'HH:mm:ss");

			if (startTimeDT.after(endTimeDT)) {
				throw new ValidationException("Start date time is greater than End date time");
			}
		}

		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void validateTransType(TransactionRequestParam requestParam) {
		if (null != requestParam.getCreditDebitFlag() && !"C".equalsIgnoreCase(requestParam.getCreditDebitFlag())
				&& !"D".equalsIgnoreCase(requestParam.getCreditDebitFlag())) {
			throw new ValidationException("Invalid value for transaction-type.");
		}
	}

	public static void validateChequeNum(TransactionRequestParam requestParam) {
		if (requestParam.getLowChequeNum() > 0) {
			if (requestParam.getHighChequeNum() <= 0) {
				throw new ValidationException("High cheque number is not provided ");
			}
		} else if (requestParam.getHighChequeNum() > 0) {
			if (requestParam.getLowChequeNum() <= 0) {
				throw new ValidationException("Low cheque number is not provided ");
			}
		}
	}

	public static void validateMaxRecord(TransactionRequestParam requestParam) {
		if (requestParam.getRecordLimit() > 4000) {
			requestParam.setRecordLimit(4000);
		}
	}

	public static void validateDescSearch(TransactionRequestParam requestParam) {
		if (null != requestParam.getDescOperator() && !requestParam.getDescOperator().isEmpty()) {

			if (requestParam.getDescOperator().equals("Contains") || requestParam.getDescOperator().equals("StartsWith")
					|| requestParam.getDescOperator().equals("EndsWith")) {
				if (requestParam.getDescSearch() == null) {
					throw new ValidationException("desc-search cannot be null if you are passing desc-operator");
				}
				if (requestParam.getDescSearch().length() < 3 || requestParam.getDescSearch().length() > 120) {
					throw new ValidationException("Invalid size for desc search");
				}
			} else {
				throw new ValidationException("Invalid value passed for desc-operator");
			}
		} else {
			if (null != requestParam.getDescSearch()) {
				throw new ValidationException("Desc-operator value is not provided");
			}
		}
	}

	public static void validateDate(TransactionRequestParam requestParam) {

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		Date startDate = requestParam.getStartDate();
		Date endDate = requestParam.getEndDate();
		Date todaysDate = calendar.getTime();

		/*
		 * Calendar calendar = Calendar.getInstance(); calendar.add(Calendar.DATE, -2);
		 */
		// calendar.getTime()
		// Date twoYearsBfDate = calculateTwoYearBeforeDate();
		// twoYearsBfDate = java.sql.Date.valueOf(currentDate.minusYears(2));

		/*
		 * if (startDate != null && endDate != null) { if
		 * (startDate.before(twoYearsBfDate) && endDate.before(twoYearsBfDate)) { throw
		 * new ValidationException(ResponseConstants.INVALID_DATE_RANGE); } }
		 */

		if (startDate == null && endDate == null) {
			calculateStartDate(requestParam);
			startDate = requestParam.getStartDate();
			calculateEndDate(requestParam);
			endDate = requestParam.getEndDate();
		} else if (startDate == null && endDate != null) {
			/*
			 * if (endDate.before(twoYearsBfDate)) { throw new
			 * ValidationException(ResponseConstants.END_DATE_IS_OLDER_THAN_TWO_YEARS_DATE);
			 * }
			 */
			if (endDate.after(todaysDate)) {
				calculateStartDate(requestParam);
				startDate = requestParam.getStartDate();
			} else {
				calculateStartDateEndDateMinus90Days(requestParam, endDate);
				startDate = requestParam.getStartDate();
			}
			if (startDate.after(endDate)) {
				throw new ValidationException(ResponseConstants.START_DATE_IS_BIGGER_THAN_END_DATE);
			}
		}

		else if (startDate != null && endDate == null) {
			/*
			 * if (startDate.before(twoYearsBfDate)) { startDate = twoYearsBfDate;
			 * requestParam.setStartDate(startDate);
			 * setEndDateToStartDatePlus90Days(requestParam, startDate, todaysDate); endDate
			 * = requestParam.getEndDate(); } else
			 */ if (/* startDate.after(twoYearsBfDate) && */ startDate.before(todaysDate)) {
				setEndDateToStartDatePlus90Days(requestParam, startDate, todaysDate);
				endDate = requestParam.getEndDate();
			} else if (startDate.equals(todaysDate)) {
				requestParam.setEndDate(todaysDate);
			} else if (startDate.after(todaysDate)) {
				throw new ValidationException(ResponseConstants.START_DATE_IS_AFTER_TODAYS_DATE);
			}
		}

		else if (startDate != null && endDate != null) {

			/*
			 * if (startDate.before(endDate) && startDate.after(twoYearsBfDate) &&
			 * startDate.before(todaysDate)) {
			 * 
			 * if (endDate.after(todaysDate) && (endDate.after(twoYearsBfDate) &&
			 * endDate.before(todaysDate))) { // Do nothing } else if
			 * (endDate.before(todaysDate) && (endDate.after(twoYearsBfDate) &&
			 * endDate.before(todaysDate))) { // Do nothing } else if
			 * (endDate.equals(todaysDate) && (endDate.after(twoYearsBfDate) &&
			 * endDate.before(todaysDate))) { // Do nothing } }
			 */

			// Cover scenario from date sheet line no 17,18,19
			/*
			 * if (startDate.before(endDate) && startDate.before(twoYearsBfDate)) { if
			 * (((endDate.before(todaysDate) && endDate.after(twoYearsBfDate)) ||
			 * endDate.equals(todaysDate) || endDate.after(todaysDate))) {
			 * requestParam.setStartDate(twoYearsBfDate); // End date will not change } }
			 */

			if (startDate.equals(endDate) && (/* startDate.after(twoYearsBfDate) && */ startDate.before(todaysDate))) {
				if (endDate.after(todaysDate) /* && endDate.after(twoYearsBfDate) */) {
					throw new ValidationException(ResponseConstants.INVALID_DATE_RANGE);
				}
				/*
				 * else if ((endDate.before(todaysDate) && endDate.after(twoYearsBfDate)) ||
				 * (endDate.equals(todaysDate) && endDate.after(twoYearsBfDate))) { // Do
				 * nothing }
				 */

			}

			else if (startDate.equals(endDate) /* && startDate.before(twoYearsBfDate) */) {
				if (endDate.after(todaysDate) /* && endDate.before(twoYearsBfDate) */) {
					// This case is not possible
				} else if (endDate.before(todaysDate)/* && endDate.before(twoYearsBfDate) */) {
					throw new ValidationException(ResponseConstants.INVALID_DATE_RANGE);
				} else if (endDate.equals(todaysDate)/* && endDate.before(twoYearsBfDate) */) {
					// This case is not possible
				}
			}

			else if (startDate.after(endDate)) {
				throw new ValidationException(ResponseConstants.START_DATE_IS_AFTER_END_DATE);
			}

			else if (startDate.after(todaysDate) && endDate.after(todaysDate)) {
				throw new ValidationException(ResponseConstants.INVALID_DATE_RANGE);
			}
		}
	}

	/*
	 * public static void validateTime(String field, String value) { try { if (null
	 * != value && !value.isEmpty()) { LocalTime.parse(value); } } catch (Exception
	 * e) { throw new ValidationException("Invalid value for " + field); }
	 * 
	 * }
	 */

	public static void validateAmount(BigDecimal minAmount, BigDecimal maxAmount) {

		if ((minAmount.compareTo(new BigDecimal(0)) == -1) || (maxAmount.compareTo(new BigDecimal(0)) == -1)) {
			throw new ValidationException(ResponseConstants.AMOUNTFILTERNEGITIVE);
		}

		if (maxAmount.compareTo(minAmount) == -1) {
			throw new ValidationException(ResponseConstants.MINAMOUNT_IS_BIGGER_THAN_MAXAMOUNT);
		}
	}

	/*
	 * public static void validateTime(TransactionRequestParam requestParam) {
	 * if(requestParam.getStartTime() != null) { if(requestParam.getEndTime() ==
	 * null) { throw new
	 * ValidationException(ResponseConstants.END_TIME_CANNOT_BE_NULL); } }
	 * if(requestParam.getEndTime() !=null) { if(requestParam.getStartTime() ==
	 * null) { throw new
	 * ValidationException(ResponseConstants.START_TIME_CANNOT_BE_NULL); } } }
	 */

	public static void validateCheuqeNo(int lowChequeNo, int highChequeNo, String productType) {

		if (highChequeNo < lowChequeNo) {
			throw new ValidationException(ResponseConstants.LOWCHEQUE_IS_BIGGER_THAN_HIGHCHEQUE);
		}
		if (productType.startsWith("PC")) {
			throw new ValidationException(ResponseConstants.CHEQUEFILTER_NOT_SUPPORTED);
		}
	}

	public static void calculateStartDate(TransactionRequestParam requestParam) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -90);
		Date startDate = calendar.getTime();
		requestParam.setStartDate(startDate);
	}

	public static void calculateStartDateEndDateMinus90Days(TransactionRequestParam requestParam, Date endDate) {
		Date startDate = java.sql.Date
				.valueOf((endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).minusDays(90));
		requestParam.setStartDate(startDate);
		/*
		 * if (startDate.before(twoYearsBfDate)) {
		 * requestParam.setStartDate(twoYearsBfDate); }
		 */
	}

	public static void calculateEndDate(TransactionRequestParam requestParam) {
		Calendar calendar = Calendar.getInstance();
		Date endDate = calendar.getTime();
		requestParam.setEndDate(endDate);
	}

	public static Date calculateTwoYearBeforeDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -730);
		// calendar.getTime()
		Date twoYearsBfDate = calendar.getTime();
		// twoYearsBfDate = java.sql.Date.valueOf(currentDate.minusYears(2));
		return twoYearsBfDate;
	}

	public static void setEndDateToStartDatePlus90Days(TransactionRequestParam requestParam, Date startDate,
			Date todaysDate) {
		Date endDate = java.sql.Date
				.valueOf((startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).plusDays(90));

		if (endDate.after(todaysDate)) {
			requestParam.setEndDate(todaysDate);
		} else {
			requestParam.setEndDate(endDate);
		}
	}

	public static void validateRequest(TransactionsRequest request) {
		validateProdType(request);

	}

	/*
	 * public static void setEndDateToTodaysPlus90Days(TransactionRequestParam
	 * requestParam, Date todayDate) { endDate = java.sql.Date
	 * .valueOf((todayDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
	 * .plusDays(90)); requestParam.setEndDate(endDate); }
	 * 
	 * public static boolean diffBetweenStartAndEndDate(Date startDate, Date
	 * endDate) {
	 * 
	 * boolean daysFlag = false;
	 * 
	 * LocalDate stDate =
	 * startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); LocalDate
	 * edDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	 * 
	 * long days = ChronoUnit.DAYS.between(stDate, edDate);
	 * 
	 * if (days > 730) { daysFlag = true; }
	 * 
	 * return daysFlag; }
	 * 
	 * public static void setStartDateEndDateMinsTwoYears(TransactionRequestParam
	 * requestParam, Date endDate) { startDate = java.sql.Date
	 * .valueOf((endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).
	 * minusDays(730)); requestParam.setStartDate(startDate); }
	 */
}
