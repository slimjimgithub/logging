package com.anz.rtl.transactions.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anz.rtl.transactions.response.TransactionsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public class JsonUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
	private ObjectMapper mapper;

	public JsonUtil() {
		mapper = new ObjectMapper().registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());
	}

	public String marshal(TransactionsResponse data) {
		String strResult = "";
		try {
			strResult = mapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			// Ignore exception
			LOGGER.error("ERROR :", e);
		}
		return strResult;
	}

	public TransactionsResponse unmarshall(String data) {
		TransactionsResponse strResult = null;
		try {
			strResult = mapper.readValue(data, TransactionsResponse.class);
		} catch (Exception e) {
			LOGGER.error("ERROR :", e);
			// Ignore exception
		}
		return strResult;
	}

}