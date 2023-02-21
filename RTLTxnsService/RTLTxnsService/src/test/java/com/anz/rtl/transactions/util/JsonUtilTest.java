package com.anz.rtl.transactions.util;

import com.anz.rtl.transactions.response.TransactionsResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class JsonUtilTest {
	private static final Logger LOG = LoggerFactory.getLogger(JsonUtilTest.class);
	@Mock
	ObjectMapper mapper;

	@InjectMocks
	JsonUtil jsonUtil;

	@Test
	public void testMarshal() {
		// String strResult = "";
		// strResult = mapper.writeValueAsString(data);
		TransactionsResponse resp = null;
		try {
			jsonUtil.marshal(resp);
		} catch (Exception e) {

		}

	}

	@Test
	public void testMarshals() throws JsonProcessingException {
		jsonUtil.marshal(Mockito.any());

	}

	@Ignore
	@Test
	public void testUnmarshall() {
		try {
			jsonUtil.unmarshall(null);
		} catch (Exception e) {

		}

	}
	
	@Test
	public void testUnMarshal() {
		// String strResult = "";
		// strResult = mapper.writeValueAsString(data);
		try {
			String data = null;
			jsonUtil.unmarshall(data);
		} catch (Exception e) {

		}

	}


}
