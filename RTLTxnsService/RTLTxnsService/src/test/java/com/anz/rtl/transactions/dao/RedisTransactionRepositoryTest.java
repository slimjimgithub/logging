package com.anz.rtl.transactions.dao;

import com.anz.rtl.transactions.request.*;
import com.anz.rtl.transactions.response.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RedisTransactionRepositoryTest {

    @InjectMocks
    RedisTransactionRepository redisTransactionRepository;

    @Mock
    RedisTemplate redisTemplate;

    @Mock
    HashOperations<String, String, TransactionsResponse> hashOperation;

    @Test
    public void saveTrasactionDetail() {
        TransactionsResponse response=new TransactionsResponse();
        TransactionAccountDetailRequest accountDetailRequest = new TransactionAccountDetailRequest();
        accountDetailRequest.setAccountId("102020");
        response.setAccountInfo(accountDetailRequest);
        TransactionsData transactionsData=new TransactionsData();
        BPayDetails bPayDetails=new BPayDetails();
        MerchantLogo merchantLogo = new MerchantLogo();
        CalInfo calInfo=new CalInfo();
        BodyRequest bodyRequest=new BodyRequest();
        bodyRequest.setBankAccountTransactionType("TEST");
        List<BodyRequest> bodyRequestList =new ArrayList<>();
        bodyRequestList.add(bodyRequest);
        calInfo.setCal(bodyRequestList);
        merchantLogo.setHeight(1);
        merchantLogo.setUrl("TEST.com");
        merchantLogo.setWidth(1);
        AdditionalMerchantDetails additionalMerchantDetails=new AdditionalMerchantDetails();

        bPayDetails.setApcaNum("1");
        bPayDetails.setBillerCode("2922");
        bPayDetails.setCrn("1");
        bPayDetails.setReceiptNbr("234");
        additionalMerchantDetails.setbPayDetails(bPayDetails);
        additionalMerchantDetails.setCal("TEST");
        additionalMerchantDetails.setMerchantId("1");
        additionalMerchantDetails.setPrimaryAddress("TEST");
        additionalMerchantDetails.setState("TEST");
        additionalMerchantDetails.setTxnEnrichmentId("TEST");
        transactionsData.setAdditionalMerchantInfo(additionalMerchantDetails);
        transactionsData.setBpayDetails(bPayDetails);
        List<TransactionsData> transactionsDataList =new ArrayList<>();
        transactionsDataList.add(transactionsData);
        response.setTransactions(transactionsDataList);
        Mockito.when(redisTemplate.opsForHash()).thenReturn(hashOperation);
        redisTransactionRepository.saveTrasactionDetail(response, "",12);

    }

   @Test
    public void getTransaction() {
        Mockito.when(redisTemplate.opsForHash()).thenReturn(hashOperation);
        redisTransactionRepository.getTransaction("TEST", "292902");
    }

    @Test
    public void constructRedisKey() {
        TransactionsRequest accountDetailRequest = new TransactionsRequest();
        accountDetailRequest.setAccountInfo(new TransactionAccountDetailRequest());
        TransactionRequestParam transactionRequestParam=new TransactionRequestParam();
        redisTransactionRepository.constructRedisKey(accountDetailRequest, transactionRequestParam);
    }
}