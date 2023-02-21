package com.anz.rtl.transactions.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@Component
public class DateTimeZoneSerializer extends StdSerializer<ZonedDateTime> {

	
	private static final long serialVersionUID = 1L;
	
	
	protected DateTimeZoneSerializer(Class<ZonedDateTime> t) {
		super(t);
		// TODO Auto-generated constructor stub
	}
	
	 public DateTimeZoneSerializer() {
	        this(null);
	    }

	 @Override
	 public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		 DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
		OffsetDateTime offDat=value.withZoneSameInstant(ZoneId.of("Australia/Melbourne")).toOffsetDateTime();
       		gen.writeString(formatter.format(offDat));
		 
	 }
	 
	 public static LocalDateTime convertStringtoLocalDateTime(String dateTime) {
			if(dateTime.substring(19).startsWith(".")) {
				dateTime = updateInputDate(dateTime);
			}
			DateTimeFormatterBuilder format=new DateTimeFormatterBuilder();
			format.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"))
			.appendOptional(DateTimeFormatter
					.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SXXX"))
			.appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
			
			return ZonedDateTime.parse(dateTime, format.toFormatter()).withZoneSameInstant(ZoneId.of("Australia/Melbourne")).toOffsetDateTime().toLocalDateTime();

		}
	 
	 public static String updateInputDate(String date) {
			if(date.substring(19).endsWith("Z")){
				int len=date.substring(19).length();
				if(date.substring(19,(date.length()-1)).matches("^.[0-9]*") && len>9) {
				String dt=date.substring(19,28);
				date=date.substring(0,19)+dt+"Z";
				}
			}else if(date.substring(date.length()-6).startsWith("+")||date.substring(date.length()-6).startsWith("-")) {
				int len=date.substring(19,(date.length()-7)).length();
				if(date.substring(19,(date.length()-6)).matches("^.[0-9]*") && len>9) {
				String dt=date.substring(19,28);
				date=date.substring(0,19)+dt+date.substring(date.length()-6);
				}
			}
			return date;
		}

	
	
}