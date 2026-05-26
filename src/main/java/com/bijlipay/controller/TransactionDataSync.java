package com.bijlipay.controller;

import com.bijlipay.txndb.model.BijlipayTransaction;
import com.bijlipay.model.dto.JupiterTxnData;
import com.bijlipay.txndb.repository.BijlipayTransactionRepository;
import com.bijlipay.service.BijlipayTransactionService;
import com.bijlipay.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/txn/sync")
public class TransactionDataSync {
    private final Logger logger = LoggerFactory.getLogger(TransactionDataSync.class);

    @Autowired
    BijlipayTransactionRepository txnRepository;

    @Autowired
    BijlipayTransactionService bijlipayTransactionService;
    @PostMapping("/jupiter-txn-data")
    public ResponseEntity<?> jupiterTxnDataSync(@RequestBody JupiterTxnData jupiterTxnData) throws ParseException {
        logger.info("JUPITER TXN DATA : {}", jupiterTxnData.toString());

        try {

        BijlipayTransaction txnData=bijlipayTransactionService.store(jupiterTxnData);
//        BijlipayTransaction txnData = new BijlipayTransaction();
//        try {
//            txnData.setMti(jupiterTxnData.getMti() != null ? jupiterTxnData.getMti() : "");
//            txnData.setTxnType(jupiterTxnData.getTxnType() != null ? jupiterTxnData.getTxnType() : "");
//            txnData.setTerminalId(jupiterTxnData.getTerminalId() != null ? jupiterTxnData.getTerminalId() : "");
//            txnData.setMerchantId(jupiterTxnData.getMerchantId() != null ? jupiterTxnData.getMerchantId() : "");
//            txnData.setTxnDate(jupiterTxnData.getTxnDate() != null ? jupiterTxnData.getTxnDate() : "");
//            txnData.setTxnTime(jupiterTxnData.getTxnTime() != null ? jupiterTxnData.getTxnTime() : "");
//            if(jupiterTxnData.getTxnAmount()!=null) {
//                String amt = jupiterTxnData.getTxnAmount();
//                long parseAmt = Long.parseLong(amt);
//                Double txnAmt = parseAmt/100.0;
//                String formattedAmt = String.format("%.2f", txnAmt);
//                txnData.setTxnAmount(formattedAmt);
//                logger.info("amt {} parsed Amt {}",amt, formattedAmt);
//            }
//            txnData.setTxnResponseCode(jupiterTxnData.getTxnResponseCode() != null ? jupiterTxnData.getTxnResponseCode() : "");
//            if (jupiterTxnData.getResponseReceivedTime() != null) {
//                String responseReceivedTime = jupiterTxnData.getResponseReceivedTime();
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date date = dateFormat.parse(responseReceivedTime);
//                txnData.setResponseReceivedTime(date);
//                txnData.setTxnCorrelationId(date);
//            }
//            txnData.setrRNumber(jupiterTxnData.getRrn() != null ? jupiterTxnData.getRrn() : "");
//            txnData.setStan(jupiterTxnData.getStan() != null ? jupiterTxnData.getStan() : "");
//            txnData.setInvoiceNumber(jupiterTxnData.getInvoiceNumber() != null ? jupiterTxnData.getInvoiceNumber() : "");
//            txnData.setBatchNumber(jupiterTxnData.getBatchNumber() != null ? jupiterTxnData.getBatchNumber() : "null");
//            txnData.setTxnAdditionalAmount(jupiterTxnData.getTxnAdditionalAmount() != null ? jupiterTxnData.getTxnAdditionalAmount() : "");
//            txnData.setMaskedcardnumber(jupiterTxnData.getMaskedCardNumber() != null ? jupiterTxnData.getMaskedCardNumber() : "");
//            txnData.setAuthCode(jupiterTxnData.getAuthCode() != null ? jupiterTxnData.getAuthCode() : "null");
//
////            txnData.setUrn("");
////            txnData.setTxnCorrelationId("");
////            txnData.setPanEntryMode("");
////            txnData.setBin("");
////            txnData.setCustomerVpa("");
//
//            txnRepository.save(txnData);

            logger.info("JUPITER TXN DATA INSERTED SUCCESSFULLY");

            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "Failure"), HttpStatus.BAD_REQUEST);
        }
    }
}
