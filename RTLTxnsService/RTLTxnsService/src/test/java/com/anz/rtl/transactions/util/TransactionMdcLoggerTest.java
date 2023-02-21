package com.anz.rtl.transactions.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionMdcLoggerTest {

    @Test
    public void testWriteToMDCLogProType() {
        TransactionMdcLogger.writeToMDCLog("1", "SUCCESS", 1l,
                "", "PC","123131313313",1,"",
                "", new Date(), new Date());
    }
    @Test
    public void TestWriteToMDCLog() {
        TransactionMdcLogger.writeToMDCLog("1", "SUCCESS", 1l,
                "", null,"123131313313",1,"",
                "", new Date(), new Date());
    }

}