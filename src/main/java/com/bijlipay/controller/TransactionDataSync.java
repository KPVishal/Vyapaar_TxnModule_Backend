package com.bijlipay.controller;

import com.bijlipay.model.dto.JupiterTxnData;
import com.bijlipay.service.BijlipayTransactionService;

import com.bijlipay.txndb.repository.BijlipayTransactionRepository;
import com.bijlipay.util.ApiResponse;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/txn/sync"})
public class TransactionDataSync {
    private final Logger logger = LoggerFactory.getLogger(TransactionDataSync.class);
    @Autowired
    private BijlipayTransactionRepository bijlipayTransactionRepository;
    @Autowired
    BijlipayTransactionService bijlipayTransactionService;

    @PostMapping({"/jupiter-txn-data"})
    public ResponseEntity<?> jupiterTxnDataSync(@RequestBody JupiterTxnData jupiterTxnData) throws ParseException {
        this.logger.info("JUPITER TXN DATA : {}", jupiterTxnData.toString());

        try {
            this.bijlipayTransactionService.store(jupiterTxnData);
            this.logger.info("JUPITER TXN DATA INSERTED SUCCESSFULLY");
            return new ResponseEntity(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "Failure"), HttpStatus.BAD_REQUEST);
        }
    }
}