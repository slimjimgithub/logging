package com.anz.rtl.transactions.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.anz.rtl.transactions.constants.TransactionConstants;
import com.anz.rtl.transactions.response.TransactionsData;
import com.anz.rtl.transactions.util.Util;

@Service
public class EnrichTransactionService {
	/**
	 * Filter(eligibleCals) eligible transactions from all 3 types of transactions
	 * (POSTED/PENDING/OUTSTANDING) for LWC detail
	 * 
	 * @param productType
	 * @param transactions
	 * @param eligibleCals
	 */

	@Value("${lwc.txn.category:CARD,BPAY,DIRECT_DEBIT}.split(',')}")
	private List<String> txnCategoryList;

	private static final Logger LOG = LoggerFactory.getLogger(EnrichTransactionService.class);

	public void getEligibleCals(String productType, List<TransactionsData> transactions,
			Map<String, String> eligibleCals) {
		String txnType = null;
		String txnCode = null;
		String txnCategory = null;
		String txnEnrichmentId = null;
		String merchantNm = null;
		String desc1 = null;
		String desc2 = null;
		String desc3 = null;
		String desc4 = null;
		BigDecimal amount = null;
		// For Credit cards
		if (productType.startsWith("PC")) {
			for (TransactionsData transaction : transactions) {
				txnType = transaction.getTransactionType();
				txnCode = transaction.getTransactionCode();
				txnCategory = transaction.getTransactionCategory();
				merchantNm = transaction.getMerchantName();
				amount = transaction.getAmount();
				desc1 = transaction.getDesc1();
				desc2 = transaction.getDesc2();

				// Only applicable for intraday
				desc4 = transaction.getBpayCal();

				switch (transaction.getSortingTransStatus()) {
				case 1: // outstanding

					if (merchantNm != null
							&& (merchantNm.endsWith("AU") || merchantNm.endsWith("AUS")
									|| merchantNm.startsWith("PAYPAL"))
							|| (merchantNm.endsWith("AUS") || merchantNm.endsWith("AU"))
							|| merchantNm.startsWith("PAYPAL")) {
						transaction.setCal(merchantNm);
						eligibleCals.put(merchantNm, null);
						if (StringUtils.isBlank(transaction.getTransactionCategory())) {
							transaction.setTransactionCategory(TransactionConstants.CARD);
						}
					}
					break;
				case 2: // intraday
					if ("2".equals(txnType) || "02".equals(txnType) && desc2 != null) {
						transaction.setCal(desc2);
						eligibleCals.put(desc2, TransactionConstants.POS);
						transaction.setTransactionCategory(TransactionConstants.CARD);
					}
					// BPAY
					if ((("6".equals(txnType) || "06".equals(txnType)) && (("48".equals(txnCode))
							|| ("9".equals(txnCode) || "09".equals(txnCode)) || ("18".equals(txnCode)))
							&& (amount.signum() == -1))) {
						transaction.setCal(desc4);
						eligibleCals.put(desc4, TransactionConstants.BPAY);
						transaction.setTransactionCategory(TransactionConstants.BPAY);
					}

					// ATM
					if ("01".equals(txnType) || "1".equals(txnType)) {
						transaction.setTransactionCategory(TransactionConstants.ATM);
					}
					break;
				case 3: // priorday

					if (txnCode != null
							&& (txnCode.equals("21") || txnCode.equals("26") || txnCode.equals("28")
									|| txnCode.equals("61") || txnCode.equals("63")) && desc1 != null
							&& !desc1.contains("ANZ BPAY") && !getDirectDebitEligibility(transaction)) {

						if (StringUtils.isBlank(transaction.getTransactionCategory())) {
							transaction.setTransactionCategory(TransactionConstants.CARD);
						}

						if ((transaction.getOriginalCurrencyAmount() == null
								|| transaction.getOriginalCurrencyAmount().isEmpty())
								&& (transaction.getConversionFee() == null
										|| transaction.getConversionFee().isEmpty())) {
							transaction.setCal(desc1);
							eligibleCals.put(desc1,
									Util.isEmptyString(txnCategory) ? TransactionConstants.POS : txnCategory);
						}
					}
					// BPAY
					if (txnCode != null && (txnCode.equals("21") || txnCode.equals("07") || txnCode.equals("7")
							|| txnCode.equals("08") || txnCode.equals("8")) && desc1.contains("ANZ BPAY")) {
						String cal = desc1.length() >= 20 ? desc1.substring(0, 20) : desc1;
						transaction.setCal(cal);
						eligibleCals.put(cal,
								Util.isEmptyString(txnCategory) ? TransactionConstants.BPAY : txnCategory);
						if (StringUtils.isBlank(transaction.getTransactionCategory())) {
							transaction.setTransactionCategory(TransactionConstants.BPAY);
						}
					}
					// DIRECT_DEBIT
					if (getDirectDebitEligibility(transaction)) {
						String cal = desc1.length() >= 40 ? desc1.substring(0, 40) : desc1;
						transaction.setCal(cal);
						eligibleCals.put(cal,
								null);
						if (StringUtils.isBlank(transaction.getTransactionCategory())) {
							transaction.setTransactionCategory(TransactionConstants.DIRECTDEBIT);
						}
					}
					// ATM
					if (StringUtils.isBlank(transaction.getTransactionCategory())) {
						if (txnCode != null && (txnCode.equals("2") || txnCode.equals("4") || txnCode.equals("6")
								|| txnCode.equals("002") || txnCode.equals("004") || txnCode.equals("006")))
							transaction.setTransactionCategory(TransactionConstants.ATM);
					}
					break;
				}
			}
		}
		// For Debit cards
		else {
			for (TransactionsData transaction : transactions) {
				txnType = transaction.getTransactionType();
				txnCode = transaction.getTransactionCode();
				txnCategory = transaction.getTransactionCategory();
				txnEnrichmentId = transaction.getTxnEnrichmentID();
				merchantNm = transaction.getMerchantName();
				amount = transaction.getAmount();
				desc1 = transaction.getDesc1();
				desc2 = transaction.getDesc2();
				desc3 = transaction.getDesc3();
				// Only applicable for intraday
				desc4 = transaction.getBpayCal();

				switch (transaction.getSortingTransStatus()) {
				case 1: // outstanding

					if (merchantNm != null
							&& (merchantNm.endsWith("AU") || merchantNm.endsWith("AUS")
									|| merchantNm.startsWith("PAYPAL"))
							|| (merchantNm.endsWith("AUS") || merchantNm.endsWith("AU"))
							|| merchantNm.startsWith("PAYPAL")) {
						transaction.setCal(merchantNm);
						eligibleCals.put(merchantNm, null);
						if (StringUtils.isBlank(transaction.getTransactionCategory())) {
							transaction.setTransactionCategory(TransactionConstants.CARD);
						}
					}
					break;
				case 2: // intraday
					if ("2".equals(txnType) || "02".equals(txnType) && desc2 != null) {
						transaction.setCal(desc2);
						eligibleCals.put(desc2, TransactionConstants.POS);
						transaction.setTransactionCategory(TransactionConstants.CARD);
					}
					// BPAY
					if (("6".equals(txnType) || "06".equals(txnType)) && (("48".equals(txnCode))
							|| ("9".equals(txnCode) || "09".equals(txnCode)) || ("18".equals(txnCode)))
							&& (amount.signum() == -1) && desc4 != null) {
						transaction.setCal(desc4);
						eligibleCals.put(desc4, TransactionConstants.BPAY);
						transaction.setTransactionCategory(TransactionConstants.BPAY);
					}
					// ATM
					if ("01".equals(txnType) || "1".equals(txnType)) {
						transaction.setTransactionCategory(TransactionConstants.ATM);
					}
					break;
				case 3: // priorday

					LOG.debug("txnCategory [{}] txnEnrichmentId [{}] ", txnCategory, txnEnrichmentId);
					if (StringUtils.isNoneBlank(txnCategory) && StringUtils.isNoneBlank(txnEnrichmentId)
							&& isEligibleTxnCategory(txnCategory)) {
						transaction.setCal(txnEnrichmentId);
						eligibleCals.put(txnEnrichmentId, txnCategory);

					} else if (StringUtils.isNoneBlank(txnCategory) && StringUtils.isBlank(txnEnrichmentId)) {
						// It is a non-eligible cal from EOD loader
					} else if (txnCode != null) {
						switch (txnCode) {

						// CARD
						case ("01105"):
						case ("01041"):
							addCalUpdateTxnCatg(eligibleCals, desc2, TransactionConstants.CARD, transaction);
							break;
						case ("05267"):
							addCalUpdateTxnCatg(eligibleCals, desc1, TransactionConstants.CARD, transaction);
							break;
						case ("05437"):
							if (null == transaction.getTransactionCategory()) {
								transaction.setTransactionCategory(TransactionConstants.CARD);
							}

							if (!((desc1 != null && desc1.toUpperCase().contains("O/S FEE"))
									|| (desc2 != null && desc2.toUpperCase().contains("O/S FEE"))
									|| (desc3 != null && desc3.toUpperCase().contains("O/S FEE")))) {
								addCalUpdateTxnCatg(eligibleCals, desc2, TransactionConstants.CARD, transaction);
							}
							break;

						// BPAY
						case ("04837"):
						case ("06517"):
						case ("05381"): {
							addCalUpdateTxnCatg(eligibleCals,
									getSubStringOrNull(transaction.getStatementDetails(), 100, 120),
									TransactionConstants.BPAY, transaction);
							break;
						}

						// ATM
						case ("01023"):
						case ("01089"):
						case ("05612"):
						case ("04761"):
						case ("04763"):
						case ("04819"):
						case ("05420"):
						case ("05443"):
						case ("05445"):
						case ("05447"):
						case ("05459"):
						case ("05461"):
						case ("05465"):
						case ("05414"):
						case ("05616"):
						case ("06505"):
						case ("06532"): {
							if (StringUtils.isBlank(transaction.getTransactionCategory())) {
								transaction.setTransactionCategory(TransactionConstants.ATM);
							}
						}
						}

						// DIRECT_DEBIT
						if (TransactionConstants.DIRECTDEBIT.equalsIgnoreCase(transaction.getObTrnType())) {
							addCalUpdateTxnCatg(eligibleCals,
									getSubStringOrNull(transaction.getStatementDetails(), 0, 20),
									TransactionConstants.DIRECTDEBIT, transaction);
						}
					}
				}
			}
		}
	}

	private String getSubStringOrNull(String desc, int startIndex, int endIndex) {
		try {
			if (null != desc && desc.length() > startIndex) {
				if (desc.length() <= endIndex) {
					endIndex = desc.length();
				}
				return desc.substring(startIndex, endIndex);
			}
		} catch (Exception e) {
			LOG.warn("Error while gettting substring :: {}", e.getMessage());
		}
		return null;
	}

	private void addCalUpdateTxnCatg(Map<String, String> eligibleCals, String calStr, String txnCategory,
			TransactionsData transaction) {
		if (null == transaction.getTransactionCategory()) {
			transaction.setTransactionCategory(txnCategory);
		}

		if (null != calStr) {
			transaction.setCal(calStr);
			eligibleCals.put(calStr, transaction.getTransactionCategory());
		}
	}

	private boolean isEligibleTxnCategory(String txnCategory) {
		return txnCategoryList.contains(txnCategory.toUpperCase());
	}

	public static boolean getDirectDebitEligibility(TransactionsData transaction) {
		if (!Util.isEmptyString(transaction.getPosEnv()) && !Util.isEmptyString(transaction.getTransactionType())
				&& (transaction.getPosEnv().equals("R") || transaction.getPosEnv().equals("I"))
				&& transaction.getTransactionType().equals("D")) {
			return true;
		}
		return false;
	}

}
