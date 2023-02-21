package com.anz.rtl.transactions.response;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModelProperty;

@Component
public class RecordControlResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9181816306315947563L;

	@ApiModelProperty(value = "Totals records returned in this message ", example = "1", required = true, position = 1)
	private int recordSent;

	@ApiModelProperty(value = "Cursor details which used to get next set of transaction which is used to pass into the next request. \r\n"
			+ "\r\n" + "Core system cursor will be as per the core system format \r\n"
			+ "CIS cursor starts with CIS as per following format \r\n"
			+ "CIS: CIS will populate the cursor value like SEQ/Record Number. \r\n" + "\r\n"
			+ "Blindspot cursor:<Cursor>CAP:2016-05-17</Cursor> indicates next call to CIS will fetch data from CTM priorday. \r\n"
			+ "\r\n"
			+ "CIS cursor:<Cursor>CIS:2015-06-274</Cursor> indicates next call to CIS will fetch data from CIS DB from position 4. \r\n"
			+ "\r\n"
			+ "Intraday data cursor: <Cursor>CTM:7#MjE5ODQ2Mw==</Cursor> indicates next call to CIS will fetch data CTM. ", example = "CIS:2018-11-062", required = false, position = 2)
	private String cursor;

	public int getRecordSent() {
		return recordSent;
	}

	public void setRecordSent(int recordSent) {
		this.recordSent = recordSent;
	}

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}
}
