package com.anz.rtl.transactions.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class DateTimeZoneSerializerTest {
	@InjectMocks
	DateTimeZoneSerializer dateTimeZoneSerializer;
	@Mock
	JsonGenerator gen;
	@Mock
	SerializerProvider provider;

	@Test(expected = NullPointerException.class)
	public void testSerialize() throws IOException {
		dateTimeZoneSerializer.serialize(null,gen,provider);
	}

	@Test
	public void testConvertStringtoLocalDateTime() {
		String dateTime = "2019-05-23T10:28:25.+10:00";
		DateTimeZoneSerializer.convertStringtoLocalDateTime(dateTime);
	}

	@Test
	public void testUpdateInputDate() {
		String dateTime = "2019-07-05T18:46:19Z";
		DateTimeZoneSerializer.updateInputDate(dateTime);

	}

}
