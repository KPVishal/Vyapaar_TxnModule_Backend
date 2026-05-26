package com.bijlipay.service.implementation;

import com.bijlipay.controller.BaseController;
import com.bijlipay.model.*;
import com.bijlipay.model.dto.*;
import com.bijlipay.model.dto.notification.NotificationDto;
import com.bijlipay.model.dto.notification.PushNotificationResponseDto;
import com.bijlipay.model.dto.notification.SettlementNotificationUpdate;
import com.bijlipay.repository.*;
import com.bijlipay.txndb.model.BijlipayTransaction;
import com.bijlipay.txndb.model.StaticQrTxn;
import com.bijlipay.txndb.repository.BijlipayTransactionRepository;
import com.bijlipay.txndb.repository.StaticQrTxnRepository;
import com.bijlipay.service.BijlipayTransactionService;
import com.bijlipay.service.FileStorageService;
import com.bijlipay.util.CardUtil;
import com.bijlipay.util.DateUtil;
import com.bijlipay.util.MailHandler;
import com.bijlipay.util.ReportUtils;
import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.persistence.Tuple;
import javax.swing.text.html.Option;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bijlipay.util.Constant.*;

@Service
public class BijlipayTransactionServiceImpl extends BaseController implements BijlipayTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(BijlipayTransactionServiceImpl.class);

    @Value("${invoice.rec.mail}")
    private String invoiceRecMail;

    @Value("${file.upload-dir}")
    String fileUploadDir;

    @Value("${file.download.url}")
    private String fileDownloadUrl;

    @Autowired
    private MailHandler mailHandler;
    @Autowired
    private BijlipayTransactionRepository bijlipayTransactionRepository;

    @Autowired
    private StaticQrTxnRepository staticQrTxnRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserTidVpaDataRepository userTidVpaDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private SettlementRequestResponseRepository settlementRequestResponseRepository;
    @Autowired
    BijlipayTransactionRepository txnRepository;


    @Override
    public List<Object[]> getSumOfTodayAndYesterdayVoidAndReversalTxnAmount(String tid, long sessionUserId) {
        List<Object[]> obj = new ArrayList<>();
        obj = bijlipayTransactionRepository.fetchSumOfVoidReversalTxnAmountSwitchTxn(tid);
        return obj;
    }

    @Override
    public List<SwitchTransactionsDto> getLastFiveSwicthTransactions(String tid) throws IOException {
        List<Tuple> switchTranDto = bijlipayTransactionRepository.findLastFiveSwitchTransaction(tid);
        return mapSwithTxnDto(switchTranDto);
    }

    @Override
    public List<SwitchTransactionsDto> getLastFiveSwicthTransactionsBasedOnUser(List<String> tid) {
        List<Tuple> switchTranDto = bijlipayTransactionRepository.findLastFiveSwitchTransactionBasedOnUser(tid);
        return mapSwithTxnDto(switchTranDto);
    }

    @Override
    public List<SwitchTransactionsDto> getLastFiveSwicthTransactionsBasedOnUserPortal(List<String> tid) {
        List<Tuple> switchTranDto = bijlipayTransactionRepository.findLastFiveSwitchTransactionBasedOnUser(tid);
        return mapSwithTxnDtoPortal(switchTranDto);
    }

    public List<SwitchTransactionsDto> mapSwithTxnDto(List<Tuple> tuples) {
        List<SwitchTransactionsDto> switchTransactionsDtos = new ArrayList<>();
        // id, mti, txnType, terminalId, merchantId, txnDate, txnTime,txnAmount, txnResponseCode,
        // responseReceivedTime, rRNumber, stan, invoiceNumber, batchNumber
        tuples.stream().forEach(l -> {
            logger.info("THe tuples : --------- : {}", l.get(9));
            SwitchTransactionsDto switchTransactionsDto1 = new SwitchTransactionsDto();
            switchTransactionsDto1.setId(l.get(0) != null ? (BigInteger) l.get(0) : null);
            switchTransactionsDto1.setMti(l.get(1) != null ? (String) l.get(1) : "");
            switchTransactionsDto1.setTxnType(l.get(2) != null ? (String) l.get(2) : "");
            switchTransactionsDto1.setTerminalId(l.get(3) != null ? (String) l.get(3) : "");
            switchTransactionsDto1.setMerchantId(l.get(4) != null ? (String) l.get(4) : "");
            switchTransactionsDto1.setTxnDate(l.get(5) != null ? (String) l.get(5) : "");
            switchTransactionsDto1.setTxnTime(l.get(6) != null ? (String) l.get(6) : "");
            switchTransactionsDto1.setTxnAmount(l.get(7) != null ? (Double) l.get(7) : 0.0);
            switchTransactionsDto1.setTxnResponseCode(l.get(8) != null ? (String) l.get(8) : null);
            switchTransactionsDto1.setResponseReceivedTime(l.get(9) != null ? DateUtil.convertToString((Date) l.get(9), "dd MMM yyyy hh.mm a") : null);
            switchTransactionsDto1.setrRNumber(l.get(10) != null ? (String) l.get(10) : "");
            switchTransactionsDto1.setStan(l.get(11) != null ? (String) l.get(11) : "");
            switchTransactionsDto1.setInvoiceNumber(l.get(12) != null ? (String) l.get(12) : "");
            switchTransactionsDto1.setBatchNumber(l.get(13) != null ? (String) l.get(13) : "");
            switchTransactionsDto1.setTxnAdditionalAmount(l.get(14) != null ? (Double) l.get(14) : 0.0);
            switchTransactionsDto1.setMaskedCardNumber(l.get(15) != null ? (String) l.get(15) : "");
            switchTransactionsDto1.setPanEntryMode(l.get(16) != null ? (String) l.get(16) : "");
            switchTransactionsDto1.setBin(l.get(17) != null ? (String) l.get(17) : "");
            switchTransactionsDto1.setCustomerVpa(l.get(18) != null ? (String) l.get(18) : "");
            switchTransactionsDtos.add(switchTransactionsDto1);
            logger.info("The transactions : ------------------------------ : {}", switchTransactionsDto1.getId());
        });
        return switchTransactionsDtos;
    }

    public List<SwitchTransactionsDto> mapSwithTxnDtoPortal(List<Tuple> tuples) {
        List<SwitchTransactionsDto> switchTransactionsDtos = new ArrayList<>();
        // id, mti, txnType, terminalId, merchantId, txnDate, txnTime,txnAmount, txnResponseCode,
        // responseReceivedTime, rRNumber, stan, invoiceNumber, batchNumber
        tuples.stream().forEach(l -> {
            logger.info("THe tuples : --------- : {}", l.get(9));
            SwitchTransactionsDto switchTransactionsDto1 = new SwitchTransactionsDto();
            switchTransactionsDto1.setId(l.get(0) != null ? (BigInteger) l.get(0) : null);
            switchTransactionsDto1.setMti(l.get(1) != null ? (String) l.get(1) : "");
            switchTransactionsDto1.setTxnType(l.get(2) != null ? (String) l.get(2) : "");
            switchTransactionsDto1.setTerminalId(l.get(3) != null ? (String) l.get(3) : "");
            switchTransactionsDto1.setMerchantId(l.get(4) != null ? (String) l.get(4) : "");
            switchTransactionsDto1.setTxnDate(l.get(5) != null ? (String) l.get(5) : "");
            switchTransactionsDto1.setTxnTime(l.get(6) != null ? (String) l.get(6) : "");
            switchTransactionsDto1.setTxnAmount(l.get(7) != null ? (Double) l.get(7) : 0.0);
            switchTransactionsDto1.setTxnResponseCode(l.get(8) != null ? (String) l.get(8) : null);
            switchTransactionsDto1.setResponseReceivedTime(l.get(9) != null ? DateUtil.convertToString((Date) l.get(9), "dd MMM yyyy hh.mm a") : null);
            switchTransactionsDto1.setrRNumber(l.get(10) != null ? (String) l.get(10) : "");
            switchTransactionsDto1.setStan(l.get(11) != null ? (String) l.get(11) : "");
            switchTransactionsDto1.setInvoiceNumber(l.get(12) != null ? (String) l.get(12) : "");
            switchTransactionsDto1.setBatchNumber(l.get(13) != null ? (String) l.get(13) : "");
            switchTransactionsDto1.setTxnAdditionalAmount(l.get(14) != null ? (Double) l.get(14) : 0.0);
            switchTransactionsDto1.setMaskedCardNumber(l.get(15) != null ? (String) l.get(15) : "");
            switchTransactionsDto1.setPanEntryMode(l.get(16) != null ? (String) l.get(16) : "");
            switchTransactionsDto1.setBin(l.get(17) != null ? (String) l.get(17) : "");
            switchTransactionsDto1.setCustomerVpa(l.get(18) != null ? (String) l.get(18) : "");
            switchTransactionsDto1.setTxnStatus(getTxnTypeStatus((String)l.get(1)+l.get(2)+l.get(8)));
            switchTransactionsDto1.setTxnMode(l.get(17) !=  null && !l.get(17).toString().equalsIgnoreCase("null") && !l.get(17).toString().equalsIgnoreCase("") ? CardUtil.getCardType(Integer.parseInt((String) l.get(17))) : "");
            switchTransactionsDtos.add(switchTransactionsDto1);
            logger.info("The transactions : ------------------------------ : {}", switchTransactionsDto1.getId());
        });
        return switchTransactionsDtos;
    }

    public Page<SwitchTransactionsDto> mapSwithTxnDtoPagination(Page<Object[]> txnDatas) {
        List<Object[]> tuples = txnDatas.getContent();
        List<SwitchTransactionsDto> switchTransactionsDtos = new ArrayList<>();
        // id, mti, txnType, terminalId, merchantId, txnDate, txnTime,txnAmount, txnResponseCode,
        // responseReceivedTime, rRNumber, stan, invoiceNumber, batchNumber
        tuples.stream().forEach(l -> {
            logger.info("THe tuples : --------- : {}", l[9]);
            SwitchTransactionsDto switchTransactionsDto1 = new SwitchTransactionsDto();
            switchTransactionsDto1.setId(l[0] != null ? (BigInteger) l[0] : null);
            switchTransactionsDto1.setMti(l[1] != null ? (String) l[1] : "");
            switchTransactionsDto1.setTxnType(l[2] != null ? (String) l[2] : "");
            switchTransactionsDto1.setTerminalId(l[3] != null ? (String) l[3] : "");
            switchTransactionsDto1.setMerchantId(l[4] != null ? (String) l[4] : "");
            switchTransactionsDto1.setTxnDate(l[5] != null ? (String) l[5] : "");
            switchTransactionsDto1.setTxnTime(l[6] != null ? (String) l[6] : "");
            switchTransactionsDto1.setTxnAmount(l[7] != null ? (Double) l[7] : 0.0);
            switchTransactionsDto1.setTxnResponseCode(l[8] != null ? (String) l[8] : null);
            switchTransactionsDto1.setResponseReceivedTime(l[9] != null ? DateUtil.convertToString((Date) l[9], "dd MMM yyyy hh.mm a") : null);
            switchTransactionsDto1.setrRNumber(l[10] != null ? (String) l[10] : "");
            switchTransactionsDto1.setStan(l[11] != null ? (String) l[11] : "");
            switchTransactionsDto1.setInvoiceNumber(l[12] != null ? (String) l[12] : "");
            switchTransactionsDto1.setBatchNumber(l[13] != null ? (String) l[13] : "");
            switchTransactionsDto1.setTxnAdditionalAmount(l[14] != null ? (Double) l[14] : 0.0);
            switchTransactionsDto1.setMaskedCardNumber(l[15] != null ? (String) l[15] : "");
            switchTransactionsDto1.setPanEntryMode(l[16] != null ? (String) l[16] : "");
            switchTransactionsDto1.setBin(l[17] != null ? (String) l[17] : "");
            switchTransactionsDto1.setCustomerVpa(l[18] != null ? (String) l[18] : "");
            switchTransactionsDtos.add(switchTransactionsDto1);
            logger.info("The transactions : ------------------------------ : {}", switchTransactionsDto1.getId());
        });
        return new PageImpl<>(switchTransactionsDtos, PageRequest.of(txnDatas.getNumber(), txnDatas.getSize(), txnDatas.getSort()), txnDatas.getTotalElements());

    }

    public Page<SwitchTransactionsDto> mapSwithTxnDtoPaginationForPortal(Page<Object[]> txnDatas) {
        List<Object[]> tuples = txnDatas.getContent();
        List<SwitchTransactionsDto> switchTransactionsDtos = new ArrayList<>();
        // id, mti, txnType, terminalId, merchantId, txnDate, txnTime,txnAmount, txnResponseCode,
        // responseReceivedTime, rRNumber, stan, invoiceNumber, batchNumber
        tuples.stream().forEach(l -> {
            logger.info("THe tuples : --------- : {}", l[9]);
            SwitchTransactionsDto switchTransactionsDto1 = new SwitchTransactionsDto();
            switchTransactionsDto1.setId(l[0] != null ? (BigInteger) l[0] : null);
            switchTransactionsDto1.setMti(l[1] != null ? (String) l[1] : "");
            switchTransactionsDto1.setTxnType(l[2] != null ? (String) l[2] : "");
            switchTransactionsDto1.setTerminalId(l[3] != null ? (String) l[3] : "");
            switchTransactionsDto1.setMerchantId(l[4] != null ? (String) l[4] : "");
            switchTransactionsDto1.setTxnDate(l[5] != null ? (String) l[5] : "");
            switchTransactionsDto1.setTxnTime(l[6] != null ? (String) l[6] : "");
            switchTransactionsDto1.setTxnAmount(l[7] != null ? (Double) l[7] : 0.0);
            switchTransactionsDto1.setTxnResponseCode(l[8] != null ? (String) l[8] : null);
            switchTransactionsDto1.setResponseReceivedTime(l[9] != null ? DateUtil.convertToString((Date) l[9], "dd MMM yyyy hh.mm a") : null);
            switchTransactionsDto1.setrRNumber(l[10] != null ? (String) l[10] : "");
            switchTransactionsDto1.setStan(l[11] != null ? (String) l[11] : "");
            switchTransactionsDto1.setInvoiceNumber(l[12] != null ? (String) l[12] : "");
            switchTransactionsDto1.setBatchNumber(l[13] != null ? (String) l[13] : "");
            switchTransactionsDto1.setTxnAdditionalAmount(l[14] != null ? (Double) l[14] : 0.0);
            switchTransactionsDto1.setMaskedCardNumber(l[15] != null ? (String) l[15] : "");
            switchTransactionsDto1.setPanEntryMode(l[16] != null ? (String) l[16] : "");
            switchTransactionsDto1.setBin(l[17] != null ? (String) l[17] : "");
            switchTransactionsDto1.setCustomerVpa(l[18] != null ? (String) l[18] : "");
            logger.info("status - -----: l1{},l2{},l8{} -------txn Status{}",l[1],l[2],l[8],getTxnTypeStatus((String)l[1]+l[2]+l[8]));
            switchTransactionsDto1.setTxnStatus(getTxnTypeStatus((String)l[1]+l[2]+l[8]));
            switchTransactionsDto1.setTxnMode(l[17] !=  null && l[17].toString().equalsIgnoreCase("null") && l[17].toString().equalsIgnoreCase("") ? CardUtil.getCardType(Integer.parseInt((String) l[17])) : "");
            switchTransactionsDtos.add(switchTransactionsDto1);
            logger.info("The transactions : ------------------------------ : {}", switchTransactionsDto1.getId());
        });
        return new PageImpl<>(switchTransactionsDtos, PageRequest.of(txnDatas.getNumber(), txnDatas.getSize(), txnDatas.getSort()), txnDatas.getTotalElements());
    }

    @Override
    public List<AxisQrTransactionDto> getLastFiveAxisQrTransactions(String vpaDetails) throws SQLException {
        List<Tuple> staticQrTxnData = staticQrTxnRepository.findLastFiveAxisStaticQrTxn(vpaDetails);
        return mapAxisQrTxnDto(staticQrTxnData);
    }

    @Override
    public List<AxisQrTransactionDto> getLastFiveAxisQrTransactionsBasedOnUser(List<String> vpaDetails) {
        List<Tuple> staticQrTxnData = staticQrTxnRepository.findLastFiveAxisStaticQrTxnBasedOnUser(vpaDetails);
        return mapAxisQrTxnDto(staticQrTxnData);
    }

    @Override
    public List<AxisQrTransactionDto> getLastFiveAxisQrTransactionsBasedOnUserPortal(List<String> vpaDetails) {
        List<Tuple> staticQrTxnData = staticQrTxnRepository.findLastFiveAxisStaticQrTxnBasedOnUser(vpaDetails);
        return mapAxisQrTxnDtoPortal(staticQrTxnData);
    }

    public List<AxisQrTransactionDto> mapAxisQrTxnDto(List<Tuple> transactionDatas) {
        List<AxisQrTransactionDto> axisQrTransactionDtos = new ArrayList<>();
        transactionDatas.forEach(l -> {
            AxisQrTransactionDto axisQrTransactionDto = new AxisQrTransactionDto();
            axisQrTransactionDto.setCustomerVpa(l.get(0) != null ? l.get(0).toString() : "");
            axisQrTransactionDto.setCreditVpa(l.get(1) != null ? l.get(1).toString() : "");
            axisQrTransactionDto.setMerchantId(l.get(2) != null ? l.get(2).toString() : "");
            axisQrTransactionDto.setMerchantChannelId(l.get(3) != null ? l.get(3).toString() : "");
            axisQrTransactionDto.setMerchantTransactionId(l.get(4) != null ? l.get(4).toString() : "");
            String txnTimeStamp = l.get(5).toString().replace(" ", "T");
            axisQrTransactionDto.setTransactionTimestamp(txnTimeStamp);
            axisQrTransactionDto.setTransactionAmount(l.get(6) != null ? l.get(6).toString() : "");
            axisQrTransactionDto.setGatewayTransactionId(l.get(7) != null ? l.get(7).toString() : "");
            axisQrTransactionDto.setGatewayResponseCode(l.get(8) != null ? l.get(8).toString() : "");
            axisQrTransactionDto.setGatewayResponseMessage(l.get(9) != null ? l.get(9).toString() : "");
            axisQrTransactionDto.setRrn(l.get(10) != null ? l.get(10).toString() : "");
            axisQrTransactionDto.setChecksum(l.get(11) != null ? l.get(11).toString() : "");
            axisQrTransactionDto.setPayeractype(l.get(12) != null ? l.get(12).toString() : "");
            axisQrTransactionDto.setPurposeCode(l.get(13) != null ? l.get(13).toString() : "");
            axisQrTransactionDto.setInitMode(l.get(14) != null ? l.get(14).toString() : "");
            axisQrTransactionDto.setCreatedAt(l.get(15) != null ? l.get(15).toString() : "");
            axisQrTransactionDtos.add(axisQrTransactionDto);
        });
        return axisQrTransactionDtos;
    }

    public List<AxisQrTransactionDto> mapAxisQrTxnDtoPortal(List<Tuple> transactionDatas) {
        List<AxisQrTransactionDto> axisQrTransactionDtos = new ArrayList<>();
        transactionDatas.forEach(l -> {
            AxisQrTransactionDto axisQrTransactionDto = new AxisQrTransactionDto();
            axisQrTransactionDto.setCustomerVpa(l.get(0) != null ? l.get(0).toString() : "");
            axisQrTransactionDto.setCreditVpa(l.get(1) != null ? l.get(1).toString() : "");
            axisQrTransactionDto.setMerchantId(l.get(2) != null ? l.get(2).toString() : "");
            axisQrTransactionDto.setMerchantChannelId(l.get(3) != null ? l.get(3).toString() : "");
            axisQrTransactionDto.setMerchantTransactionId(l.get(4) != null ? l.get(4).toString() : "");
            String txnTimeStamp = l.get(5).toString().replace(" ", "T");
            axisQrTransactionDto.setTransactionTimestamp(txnTimeStamp);
            axisQrTransactionDto.setTransactionAmount(l.get(6) != null ? l.get(6).toString() : "");
            axisQrTransactionDto.setGatewayTransactionId(l.get(7) != null ? l.get(7).toString() : "");
            axisQrTransactionDto.setGatewayResponseCode(l.get(8) != null ? l.get(8).toString() : "");
            axisQrTransactionDto.setGatewayResponseMessage(l.get(9) != null ? l.get(9).toString() : "");
            axisQrTransactionDto.setRrn(l.get(10) != null ? l.get(10).toString() : "");
            axisQrTransactionDto.setChecksum(l.get(11) != null ? l.get(11).toString() : "");
            axisQrTransactionDto.setPayeractype(l.get(12) != null ? l.get(12).toString() : "");
            axisQrTransactionDto.setPurposeCode(l.get(13) != null ? l.get(13).toString() : "");
            axisQrTransactionDto.setInitMode(l.get(14) != null ? l.get(14).toString() : "");
            axisQrTransactionDto.setCreatedAt(l.get(15) != null ? l.get(15).toString() : "");
            axisQrTransactionDto.setTxnMode("UPI");
            axisQrTransactionDto.setTxnStatus(l.get(8) != null && l.get(8).toString().equalsIgnoreCase("00") ? "Success" : l.get(9).toString());
            axisQrTransactionDtos.add(axisQrTransactionDto);
        });
        return axisQrTransactionDtos;
    }

    public Page<AxisQrTransactionDto> mapAxisQrTxnDtoPagination(Page<Object[]> transactionDatas) {
        List<Object[]> content = transactionDatas.getContent();
        List<AxisQrTransactionDto> axisQrTransactionDtos = new ArrayList<>();
        content.forEach(l -> {
            AxisQrTransactionDto axisQrTransactionDto = new AxisQrTransactionDto();
            axisQrTransactionDto.setCustomerVpa(l[0] != null ? l[0].toString() : "");
            axisQrTransactionDto.setCreditVpa(l[1] != null ? l[1].toString() : "");
            axisQrTransactionDto.setMerchantId(l[2] != null ? l[2].toString() : "");
            axisQrTransactionDto.setMerchantChannelId(l[3] != null ? l[3].toString() : "");
            axisQrTransactionDto.setMerchantTransactionId(l[4] != null ? l[4].toString() : "");
            String txnTimeStamp = l[5].toString().replace(" ", "T");
            axisQrTransactionDto.setTransactionTimestamp(txnTimeStamp);
            axisQrTransactionDto.setTransactionAmount(l[6] != null ? l[6].toString() : "");
            axisQrTransactionDto.setGatewayTransactionId(l[7] != null ? l[7].toString() : "");
            axisQrTransactionDto.setGatewayResponseCode(l[8] != null ? l[8].toString() : "");
            axisQrTransactionDto.setGatewayResponseMessage(l[9] != null ? l[9].toString() : "");
            axisQrTransactionDto.setRrn(l[10] != null ? l[10].toString() : "");
            axisQrTransactionDto.setChecksum(l[11] != null ? l[11].toString() : "");
            axisQrTransactionDto.setPayeractype(l[12] != null ? l[12].toString() : "");
            axisQrTransactionDto.setPurposeCode(l[13] != null ? l[13].toString() : "");
            axisQrTransactionDto.setInitMode(l[14] != null ? l[14].toString() : "");
            axisQrTransactionDto.setCreatedAt(l[15] != null ? l[15].toString() : "");
            axisQrTransactionDtos.add(axisQrTransactionDto);
        });
        return new PageImpl<>(axisQrTransactionDtos, PageRequest.of(transactionDatas.getNumber(), transactionDatas.getSize(), transactionDatas.getSort()), transactionDatas.getTotalElements());
    }


    @Override
    public List<Object[]> getSumAndCountOfTodayAndYesterdayTxnAmountForTid(String tid, long sessionUserId) {
        List<Object[]> result = new ArrayList<>();
        Object[] data = new Object[4];

        // --- Consolidated: 1 DB round-trip instead of 5 ---
        List<Object[]> consolidated = bijlipayTransactionRepository.fetchConsolidatedTodayYesterdaySums(tid);

        // Parse consolidated rows: each row = [mti, txn_type, today_sum, today_count, yesterday_sum, yesterday_count]
        double todaySumPos200 = 0, todaySumPos400 = 0, todaySumVoid = 0, todaySumUpi37 = 0, todaySumUpi39 = 0;
        long todayCountPos200 = 0, todayCountPos400 = 0, todayCountVoid = 0, todayCountUpi37 = 0, todayCountUpi39 = 0;
        double yesterdaySumPos200 = 0, yesterdaySumPos400 = 0, yesterdaySumVoid = 0, yesterdaySumUpi37 = 0, yesterdaySumUpi39 = 0;
        long yesterdayCountPos200 = 0, yesterdayCountPos400 = 0, yesterdayCountVoid = 0, yesterdayCountUpi37 = 0, yesterdayCountUpi39 = 0;

        for (Object[] row : consolidated) {
            String mti = row[0] != null ? row[0].toString() : "";
            String txnType = row[1] != null ? row[1].toString() : "";
            double tSum = row[2] != null ? ((Number) row[2]).doubleValue() : 0;
            long tCount = row[3] != null ? ((Number) row[3]).longValue() : 0;
            double ySum = row[4] != null ? ((Number) row[4]).doubleValue() : 0;
            long yCount = row[5] != null ? ((Number) row[5]).longValue() : 0;

            if ("0200".equals(mti) && "00".equals(txnType)) {
                todaySumPos200 = tSum; todayCountPos200 = tCount; yesterdaySumPos200 = ySum; yesterdayCountPos200 = yCount;
            } else if ("0200".equals(mti) && "02".equals(txnType)) {
                todaySumVoid = tSum; todayCountVoid = tCount; yesterdaySumVoid = ySum; yesterdayCountVoid = yCount;
            } else if ("0400".equals(mti) && "00".equals(txnType)) {
                todaySumPos400 = tSum; todayCountPos400 = tCount; yesterdaySumPos400 = ySum; yesterdayCountPos400 = yCount;
            } else if ("0100".equals(mti) && "37".equals(txnType)) {
                todaySumUpi37 = tSum; todayCountUpi37 = tCount; yesterdaySumUpi37 = ySum; yesterdayCountUpi37 = yCount;
            } else if ("0100".equals(mti) && "39".equals(txnType)) {
                todaySumUpi39 = tSum; todayCountUpi39 = tCount; yesterdaySumUpi39 = ySum; yesterdayCountUpi39 = yCount;
            }
        }

        double today_sum_card = todaySumPos200 - todaySumPos400 - todaySumVoid;
        long today_count_card = todayCountPos200 - todayCountPos400 - todayCountVoid;
        double yesterday_sum_card = yesterdaySumPos200 - yesterdaySumPos400 - yesterdaySumVoid;
        long yesterday_count_card = yesterdayCountPos200 - yesterdayCountPos400 - yesterdayCountVoid;

        double today_sum_upi = todaySumUpi37 + todaySumUpi39;
        long today_count_upi = todayCountUpi37 + todayCountUpi39;
        double yesterday_sum_upi = yesterdaySumUpi37 + yesterdaySumUpi39;
        long yesterday_count_upi = yesterdayCountUpi37 + yesterdayCountUpi39;

        data[0] = today_sum_card + today_sum_upi;
        data[1] = BigInteger.valueOf(today_count_card + today_count_upi);
        data[2] = yesterday_sum_card + yesterday_sum_upi;
        data[3] = BigInteger.valueOf(yesterday_count_card + yesterday_count_upi);
        result.add(data);
        return result;
    }

    @Override
    public List<Object[]> getSumAndCountOfTodayAndYesterdayTxnAmountForVpa(String vpa, long sessionUserId) {
        return staticQrTxnRepository.fetchSumAndCountOfTodayAndYesterdayTxn(vpa);
    }

    @Override
    public Page<AxisQrTransactionDto> getAxisQrTransactionDetails(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<List<String>> vpa, Pageable pageable) throws ParseException {
        if (action.isPresent()) {
            switch (value.get()) {
                case DATE_RANGE_THIS_MONTH: {
                    return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrTxnThisMonthData(vpaDetails.get(0), pageable));
                }
                case DATE_RANGE_LAST_30_DAYS: {
                    return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrTxnLast30DaysData(vpaDetails, pageable));
                }
                case DATE_RANGE_CHOOSE_DATE: {
                    if (vpa.isPresent()) {
                        if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", vfrom, vto, vpaDetails.get(0), searchTerm.get());
                            return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTerm(vfrom, vto, vpa.get(), searchTerm.get(), pageable));
                        } else if (from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", vfrom, vto, vpaDetails.get(0));
                            return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrBasedOnVpaWithDates(vfrom, vto, vpa.get(), pageable));
                        } else if (searchTerm.isPresent()) {
                            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                            return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm(vpa.get(), searchTerm.get(), pageable));
                        }
                    } else {
                        if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", vfrom, vto, vpaDetails.get(0), searchTerm.get());
                            return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTerm(vfrom, vto, Collections.singletonList(vpaDetails.get(0)), searchTerm.get(), pageable));
                        } else if (from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", vfrom, vto, vpaDetails.get(0));
                            return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrBasedOnVpaWithDates(vfrom, vto, Collections.singletonList(vpaDetails.get(0)), pageable));
                        } else if (searchTerm.isPresent()) {
                            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                            return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm(Collections.singletonList(vpaDetails.get(0)), searchTerm.get(), pageable));
                        }
                    }
                }
            }
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrTxn(Collections.singletonList(vpaDetails.get(0)), pageable));
        } else if (searchTerm.isPresent()) {
            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
            return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm(vpa.orElseGet(() -> Collections.singletonList(vpaDetails.get(0))), searchTerm.get(), pageable));
        } else {
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return mapAxisQrTxnDtoPagination(staticQrTxnRepository.findAxisQrTxn(vpa.orElseGet(() -> Collections.singletonList(vpaDetails.get(0))), pageable));
        }
    }

//    @Override
//    public Page<SwitchTransactionsDto> getSwitchTransactionsSearch(Optional<Integer> action, List<String> tidList, Optional<String> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm, Pageable pageable) throws ParseException {
//        logger.info("SWITCH_TXN : entered into the section :");
//
//        if (action.isPresent()) {
//            if (action.get() == TXN_RESPONSE_CODE) {
//                logger.info("ACTION ------------------------: {}", action.get());
//                if (requestDto.getTxnStatus() != 0) {
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearch(tid.get(), searchTerm.get(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailureSearch(tid.get(), searchTerm.get(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch(tid.get(), searchTerm.get(), pageable));
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccess(tid.get(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailure(tid.get(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailure(tid.get(), pageable));
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearch(tidList.get(0), searchTerm.get(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailureSearch(tidList.get(0), searchTerm.get(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch(tidList.get(0), searchTerm.get(), pageable));
//                        }
//                    } else {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccess(tidList.get(0), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailure(tidList.get(0), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailure(tidList.get(0), pageable));
//                        }
//                    }
//                }
//            } else if (action.get() == TXN_TYPE) {
//                logger.info("ACTION ------------------------: {}", action.get());
//                if (!requestDto.getTxnType().isEmpty()) {
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearch(tid.get(), requestDto.getTxnType(), searchTerm.get(), pageable));
//                    } else if (tid.isPresent()) {
//                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnType(tid.get(), requestDto.getTxnType(), pageable));
//                    } else if (searchTerm.isPresent()) {
//                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearch(tidList.get(0), requestDto.getTxnType(), searchTerm.get(), pageable));
//                    } else {
//                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnType(tidList.get(0), requestDto.getTxnType(), pageable));
//                    }
//                }
//            } else if (action.get() == DATE_RANGE) {
//                logger.info("ACTION ------------------------: {}", action.get());
//                if (requestDto.getDateRange() != 0) {
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearch(tid.get(), vfrom, vto, searchTerm.get(), pageable));
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithDateRange3(tid.get(), vfrom, vto, pageable));
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearch(tidList.get(0), vfrom, vto, searchTerm.get(), pageable));
//                        }
//                    } else {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithDateRange3(tidList.get(0), vfrom, vto, pageable));
//                        }
//                    }
//                }
//            } else if (action.get() == ALL_FILTER) {
//                logger.info("ACTION ------------------------: {}", action.get());
//                if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
//                    logger.info(" ALL ACTION ------------------------: {}", action.get());
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                                if (requestDto.getTxnType().size() > 2) {
//                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                    logger.info("requestDto txnType all 3 filter tid and search------- : {}", requestDto.getTxnType());
//                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                                    }
//                                } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                    return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                                }
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                            }
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3(tid.get(), requestDto.getTxnType(), vfrom, vto, pageable));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                                if (requestDto.getTxnType().size() > 2) {
//                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                    logger.info("requestDto txnType all 3 filter tid------- : {}", requestDto.getTxnType());
//                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3848(tid.get(), requestDto.getTxnType(), vfrom, vto, pageable));
//                                    }
//                                } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                    return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848(tid.get(), requestDto.getTxnType(), vfrom, vto, pageable));
//                                }
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3(tid.get(), requestDto.getTxnType(), vfrom, vto, pageable));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3(tid.get(), requestDto.getTxnType(), vfrom, vto, pageable));
//                            }
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch(tidList.get(0), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                                if (requestDto.getTxnType().size() > 2) {
//                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                    logger.info("requestDto txnType all 3 filter search------- : {}", requestDto.getTxnType());
//                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848(tidList.get(0), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                                    }
//                                } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                    return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848(tidList.get(0), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                                }
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch(tidList.get(0), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch(tidList.get(0), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                            }
//                        }
//                    } else {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3(tidList.get(0), requestDto.getTxnType(), vfrom, vto, pageable));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                                if (requestDto.getTxnType().size() > 2) {
//                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                    logger.info("requestDto txnType all 3 filter without tid and search------- : {}", requestDto.getTxnType());
//                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3848(tidList.get(0), requestDto.getTxnType(), vfrom, vto, pageable));
//                                    }
//                                } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                    return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848(tidList.get(0), requestDto.getTxnType(), vfrom, vto, pageable));
//                                }
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3(tidList.get(0), requestDto.getTxnType(), vfrom, vto, pageable));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3(tidList.get(0), requestDto.getTxnType(), vfrom, vto, pageable));
//                            }
//                        }
//                    }
//                } else if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty()) {
//                    logger.info(" ACTION txn status and txn type------------------------: {}", action.get());
//
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch(tid.get(), requestDto.getTxnType(), searchTerm.get(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            if (requestDto.getTxnType().size() > 2) {
//                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                    logger.info("requestDto txnType any 2 filter tid and search------- : {}", requestDto.getTxnType());
//                                    return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848(tid.get(), requestDto.getTxnType(), searchTerm.get(), pageable));
//                                }
//                            } else if (requestDto.getTxnType().size() == 2 && (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48"))) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848(tid.get(), requestDto.getTxnType(), searchTerm.get(), pageable));
//                            }
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch(tid.get(), requestDto.getTxnType(), searchTerm.get(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch(tid.get(), requestDto.getTxnType(), searchTerm.get(), pageable));
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndType(tid.get(), requestDto.getTxnType(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            if (requestDto.getTxnType().size() > 2) {
//                                requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                logger.info("requestDto txnType any 2 filter tid------- : {}", requestDto.getTxnType());
//                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                    return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType3848(tid.get(), requestDto.getTxnType(), pageable));
//                                }
//                            } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848(tid.get(), requestDto.getTxnType(), pageable));
//                            }
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType(tid.get(), requestDto.getTxnType(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType(tid.get(), requestDto.getTxnType(), pageable));
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch(tidList.get(0), requestDto.getTxnType(), searchTerm.get(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            if (requestDto.getTxnType().size() > 2) {
//                                requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848(tidList.get(0), requestDto.getTxnType(), searchTerm.get(), pageable));
//                            } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848(tidList.get(0), requestDto.getTxnType(), searchTerm.get(), pageable));
//                            }
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch(tidList.get(0), requestDto.getTxnType(), searchTerm.get(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch(tidList.get(0), requestDto.getTxnType(), searchTerm.get(), pageable));
//                        }
//                    } else {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndType(tidList.get(0), requestDto.getTxnType(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            if (requestDto.getTxnType().size() > 2) {
//                                requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                logger.info("requestDto txnType any 2 filter without tid and search------- : {}", requestDto.getTxnType());
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType3848(tidList.get(0), requestDto.getTxnType(), pageable));
//                            } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848(tidList.get(0), requestDto.getTxnType(), pageable));
//                            }
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType(tidList.get(0), requestDto.getTxnType(), pageable));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType(tidList.get(0), requestDto.getTxnType(), pageable));
//                        }
//                    }
//                } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
//                    logger.info(" ACTION txn status and date range------------------------: {}", action.get());
//                    String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                    String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch(tid.get(), vfrom, vto, searchTerm.get(), pageable));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch(tid.get(), vfrom, vto, searchTerm.get(), pageable));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch(tid.get(), vfrom, vto, searchTerm.get(), pageable));
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3(tid.get(), vfrom, vto, pageable));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3(tid.get(), vfrom, vto, pageable));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3(tid.get(), vfrom, vto, pageable));
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch(tidList.get(0), vfrom, vto, searchTerm.get(), pageable));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch(tidList.get(0), vfrom, vto, searchTerm.get(), pageable));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch(tidList.get(0), vfrom, vto, searchTerm.get(), pageable));
//                        }
//                    } else {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3(tidList.get(0), vfrom, vto, pageable));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3(tidList.get(0), vfrom, vto, pageable));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3(tidList.get(0), vfrom, vto, pageable));
//                        }
//
//                    }
//                } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
//                    logger.info(" ACTION txn type and date range------------------------: {}", action.get());
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3(tid.get(), requestDto.getTxnType(), vfrom, vto, pageable));
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch(tidList.get(0), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
//                        }
//                    } else {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3(tidList.get(0), requestDto.getTxnType(), vfrom, vto, pageable));
//                        }
//                    }
//                }
//            }
//        } else if (searchTerm.isPresent()) {
//            logger.info("WITHOUT ACTION------------------------:");
//            if (tid.isPresent()) {
//                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findAllTransactionDataWithSearch(tid.get(), searchTerm.get(), pageable));
//            }
//            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findAllTransactionDataWithSearch(tidList.get(0), searchTerm.get(), pageable));
//        } else {
//            logger.info("WITHOUT ACTION------------------------:");
//            if (tid.isPresent()) {
//                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findAllTransactionData(tid.get(), pageable));
//            }
//            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findAllTransactionData(tidList.get(0), pageable));
//        }
//        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findAllTransactionData(tidList.get(0), pageable));
//    }

    @Override
    public Page<SwitchTransactionsDto> getSwitchTransactionsSearch(Optional<Integer> action, List<String> tidList, Optional<List<String>> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm, Pageable pageable) throws ParseException {
        logger.info("SWITCH_TXN : entered into the section :");

        if (action.isPresent()) {
            if (action.get() == TXN_RESPONSE_CODE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailureSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccess(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailure(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailure(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), pageable));
                        }
                    }
                }
            } else if (action.get() == TXN_TYPE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (!requestDto.getTxnType().isEmpty()) {
                    if (searchTerm.isPresent()) {
                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                    } else {
                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnType(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                    }
                }
            } else if (action.get() == DATE_RANGE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getDateRange() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, pageable));
                        }
                    }
                }
            } else if (action.get() == ALL_FILTER) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ALL ACTION ------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                                if (requestDto.getTxnType().size() > 2) {
                                    logger.info("requestDto txnType all 3 filter search------- : {}", requestDto.getTxnType());
                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                        requestDto.getTxnType().removeAll(List.of("38", "48"));
                                        logger.info("after removing 1-----------------:{}", requestDto.getTxnType());
                                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                                    }
                                } else if (requestDto.getTxnType().size() == 2 && (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48"))) {
                                    return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                                }
                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                            }
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                                if (requestDto.getTxnType().size() > 2) {
                                    logger.info("requestDto txnType all 3 filter without tid and search------- : {}", requestDto.getTxnType());
                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                        requestDto.getTxnType().removeAll(List.of("38", "48"));
                                        logger.info("after removing 2-----------------:{}", requestDto.getTxnType());
                                        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                                    }
                                } else if (requestDto.getTxnType().size() == 2 && (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48"))) {
                                    return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                                }
                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                            }
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty()) {
                    logger.info(" ACTION txn status and txn type------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            logger.info("txn type --------------------- : {}, ------ {}", requestDto.getTxnType().size(), requestDto.getTxnType());
                            if (requestDto.getTxnType().size() > 2) {
                                logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("after removing 3----------------:{}", requestDto.getTxnType());
                                    return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                                }
                            } else if (requestDto.getTxnType().size() == 2 && (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48"))) {
                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                            }
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndType(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            logger.info("requestDto txnType any 2 filter without tid and search------- : {}", requestDto.getTxnType());
                            if (requestDto.getTxnType().size() > 2) {
                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("after removing 4-----------------:{}", requestDto.getTxnType());
                                    return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                                }
                            } else if (requestDto.getTxnType().size() == 2 && (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48"))) {
                                return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                            }
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn status and date range------------------------: {}", action.get());
                    String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                    String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get(), pageable));
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get(), pageable));
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, pageable));
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, pageable));
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, pageable));
                        }

                    }
                } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn type and date range------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                        }
                    }
                }
            }
        } else if (searchTerm.isPresent()) {
            logger.info("WITHOUT ACTION------------------------:");
            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findAllTransactionDataWithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get(), pageable));
        } else {
            logger.info("WITHOUT ACTION------------------------:");
            return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findAllTransactionData(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), pageable));
        }
        return mapSwithTxnDtoPagination(bijlipayTransactionRepository.findAllTransactionData(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), pageable));
    }

    @Override
    public Page<SwitchTransactionsDto> getSwitchTransactionsSearchData(Optional<Integer> action, List<String> tidList, Optional<List<String>> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm, Pageable pageable) throws ParseException {
        logger.info("SWITCH_TXN : entered into the section :");

        if (action.isPresent()) {
            if (action.get() == TXN_RESPONSE_CODE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailureSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccess(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailure(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailure(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), pageable));
                        }
                    }
                }
            } else if (action.get() == TXN_TYPE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (!requestDto.getTxnType().isEmpty()) {
                    if (searchTerm.isPresent()) {
                        return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                    } else {
                        return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnType(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                    }
                }
            } else if (action.get() == DATE_RANGE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getDateRange() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataWithDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, pageable));
                        }
                    }
                }
            } else if (action.get() == ALL_FILTER) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ALL ACTION ------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                                return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                                if (requestDto.getTxnType().size() > 2) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("requestDto txnType all 3 filter search------- : {}", requestDto.getTxnType());
                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                        return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                                    }
                                } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                                }
                                return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                                return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                            }
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                                return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                                if (requestDto.getTxnType().size() > 2) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("requestDto txnType all 3 filter without tid and search------- : {}", requestDto.getTxnType());
                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                        return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                                    }
                                } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
                                    return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                                }
                                return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                                return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                            }
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty()) {
                    logger.info(" ACTION txn status and txn type------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
                            if (requestDto.getTxnType().size() > 2) {
                                requestDto.getTxnType().removeAll(List.of("38", "48"));
                                return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                            } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
                                return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                            }
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndType(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
                            if (requestDto.getTxnType().size() > 2) {
                                requestDto.getTxnType().removeAll(List.of("38", "48"));
                                logger.info("requestDto txnType any 2 filter without tid and search------- : {}", requestDto.getTxnType());
                                return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                            } else if (requestDto.getTxnType().size() == 2 && (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48"))) {
                                return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                            }
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), pageable));
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn status and date range------------------------: {}", action.get());
                    String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                    String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get(), pageable));
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get(), pageable));
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, pageable));
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, pageable));
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, pageable));
                        }

                    }
                } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn type and date range------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                        }
                    }
                }
            }
        } else if (searchTerm.isPresent()) {
            logger.info("WITHOUT ACTION------------------------:");
            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findAllTransactionDataWithSearch(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get(), pageable));
        } else {
            logger.info("WITHOUT ACTION------------------------:");
            return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findAllTransactionData(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), pageable));
        }
        return mapSwithTxnDtoPaginationForPortal(bijlipayTransactionRepository.findAllTransactionData(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), pageable));
    }

//    @Override
//    public SXSSFWorkbook downloadTransactionReport(Optional<Integer> action, List<String> tidList, Optional<String> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm) throws IOException, ParseException {
//        List<Object[]> transactionsDtos = new ArrayList<>();
//        if (action.isPresent()) {
//            if (action.get() == TXN_RESPONSE_CODE) {
//                logger.info("ACTION ------------------------: {}", action.get());
//                if (requestDto.getTxnStatus() != 0) {
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearch(tid.get(), searchTerm.get()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailureSearch(tid.get(), searchTerm.get()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch(tid.get(), searchTerm.get()));
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccess(tid.get()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailure(tid.get()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailure(tid.get()));
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearch(tidList.get(0), searchTerm.get()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailureSearch(tidList.get(0), searchTerm.get()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch(tidList.get(0), searchTerm.get()));
//                        }
//                    } else {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccess(tidList.get(0)));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailure(tidList.get(0)));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailure(tidList.get(0)));
//                        }
//                    }
//                }
//            } else if (action.get() == TXN_TYPE) {
//                logger.info("ACTION ------------------------: {}", action.get());
//                if (!requestDto.getTxnType().isEmpty()) {
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearch(tid.get(), requestDto.getTxnType(), searchTerm.get()));
//                    } else if (tid.isPresent()) {
//                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnType(tid.get(), requestDto.getTxnType()));
//                    } else if (searchTerm.isPresent()) {
//                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearch(tidList.get(0), requestDto.getTxnType(), searchTerm.get()));
//                    } else {
//                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnType(tidList.get(0), requestDto.getTxnType()));
//                    }
//                }
//            } else if (action.get() == DATE_RANGE) {
//                logger.info("ACTION ------------------------: {}", action.get());
//                if (requestDto.getDateRange() != 0) {
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearch(tid.get(), vfrom, vto, searchTerm.get()));
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithDateRange3(tid.get(), vfrom, vto));
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearch(tidList.get(0), vfrom, vto, searchTerm.get()));
//                        }
//                    } else {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithDateRange3(tidList.get(0), vfrom, vto));
//                        }
//                    }
//                }
//            } else if (action.get() == ALL_FILTER) {
//                logger.info("ACTION ------------------------: {}", action.get());
//                if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
//                    logger.info(" ALL ACTION ------------------------: {}", action.get());
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get()));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                                if (requestDto.getTxnType().size() > 2) {
//                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                    logger.info("requestDto txnType all 3 filter tid and search------- : {}", requestDto.getTxnType());
//                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                        return updateQRWorkbook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get()));
//                                    }
//                                } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                    return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get()));
//                                }
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get()));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get()));
//                            }
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3(tid.get(), requestDto.getTxnType(), vfrom, vto));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                                if (requestDto.getTxnType().size() > 2) {
//                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                    logger.info("requestDto txnType all 3 filter tid------- : {}", requestDto.getTxnType());
//                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3848(tid.get(), requestDto.getTxnType(), vfrom, vto));
//                                    }
//                                } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                    return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848(tid.get(), requestDto.getTxnType(), vfrom, vto));
//                                }
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3(tid.get(), requestDto.getTxnType(), vfrom, vto));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3(tid.get(), requestDto.getTxnType(), vfrom, vto));
//                            }
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch(tidList.get(0), requestDto.getTxnType(), vfrom, vto, searchTerm.get()));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                                if (requestDto.getTxnType().size() > 2) {
//                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                    logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
//                                    return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848(tidList.get(0), requestDto.getTxnType(), searchTerm.get()));
//                                } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                    return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848(tidList.get(0), requestDto.getTxnType(), searchTerm.get()));
//                                }
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch(tidList.get(0), requestDto.getTxnType(), vfrom, vto, searchTerm.get()));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch(tidList.get(0), requestDto.getTxnType(), vfrom, vto, searchTerm.get()));
//                            }
//                        }
//                    } else {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3(tidList.get(0), requestDto.getTxnType(), vfrom, vto));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                                if (requestDto.getTxnType().size() > 2) {
//                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                    logger.info("requestDto txnType all 3 filter without tid and search------- : {}", requestDto.getTxnType());
//                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3848(tidList.get(0), requestDto.getTxnType(), vfrom, vto));
//                                    }
//                                } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                    return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848(tidList.get(0), requestDto.getTxnType(), vfrom, vto));
//                                }
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3(tidList.get(0), requestDto.getTxnType(), vfrom, vto));
//                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3(tidList.get(0), requestDto.getTxnType(), vfrom, vto));
//                            }
//                        }
//                    }
//                } else if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty()) {
//                    logger.info(" ACTION txn status and txn type------------------------: {}", action.get());
//
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch(tid.get(), requestDto.getTxnType(), searchTerm.get()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            if (requestDto.getTxnType().size() > 2) {
//                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                    logger.info("requestDto txnType any 2 filter tid and search------- : {}", requestDto.getTxnType());
//                                    return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848(tid.get(), requestDto.getTxnType(), searchTerm.get()));
//                                }
//                            } else if (requestDto.getTxnType().size() == 2 && (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48"))) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848(tid.get(), requestDto.getTxnType(), searchTerm.get()));
//                            }
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch(tid.get(), requestDto.getTxnType(), searchTerm.get()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch(tid.get(), requestDto.getTxnType(), searchTerm.get()));
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndType(tid.get(), requestDto.getTxnType()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            if (requestDto.getTxnType().size() > 2) {
//                                requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                logger.info("requestDto txnType any 2 filter tid------- : {}", requestDto.getTxnType());
//                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
//                                    return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType3848(tid.get(), requestDto.getTxnType()));
//                                }
//                            } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848(tid.get(), requestDto.getTxnType()));
//                            }
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType(tid.get(), requestDto.getTxnType()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType(tid.get(), requestDto.getTxnType()));
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch(tidList.get(0), requestDto.getTxnType(), searchTerm.get()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            if (requestDto.getTxnType().size() > 2) {
//                                requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848(tidList.get(0), requestDto.getTxnType(), searchTerm.get()));
//                            } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848(tidList.get(0), requestDto.getTxnType(), searchTerm.get()));
//                            }
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch(tidList.get(0), requestDto.getTxnType(), searchTerm.get()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch(tidList.get(0), requestDto.getTxnType(), searchTerm.get()));
//                        }
//                    } else {
//                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndType(tidList.get(0), requestDto.getTxnType()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            if (requestDto.getTxnType().size() > 2) {
//                                requestDto.getTxnType().removeAll(List.of("38", "48"));
//                                logger.info("requestDto txnType any 2 filter without tid and search------- : {}", requestDto.getTxnType());
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType3848(tidList.get(0), requestDto.getTxnType()));
//                            } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
//                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848(tidList.get(0), requestDto.getTxnType()));
//                            }
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType(tidList.get(0), requestDto.getTxnType()));
//                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType(tidList.get(0), requestDto.getTxnType()));
//                        }
//                    }
//                } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
//                    logger.info(" ACTION txn status and date range------------------------: {}", action.get());
//                    String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                    String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch(tid.get(), vfrom, vto, searchTerm.get()));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch(tid.get(), vfrom, vto, searchTerm.get()));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch(tid.get(), vfrom, vto, searchTerm.get()));
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3(tid.get(), vfrom, vto));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3(tid.get(), vfrom, vto));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3(tid.get(), vfrom, vto));
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch(tidList.get(0), vfrom, vto, searchTerm.get()));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch(tidList.get(0), vfrom, vto, searchTerm.get()));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch(tidList.get(0), vfrom, vto, searchTerm.get()));
//                        }
//                    } else {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3(tidList.get(0), vfrom, vto));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3(tidList.get(0), vfrom, vto));
//                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3(tidList.get(0), vfrom, vto));
//                        }
//
//                    }
//                } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
//                    logger.info(" ACTION txn type and date range------------------------: {}", action.get());
//                    if (tid.isPresent() && searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch(tid.get(), requestDto.getTxnType(), vfrom, vto, searchTerm.get()));
//                        }
//                    } else if (tid.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3(tid.get(), requestDto.getTxnType(), vfrom, vto));
//                        }
//                    } else if (searchTerm.isPresent()) {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch(tidList.get(0), requestDto.getTxnType(), vfrom, vto, searchTerm.get()));
//                        }
//                    } else {
//                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
//                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
//                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
//                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3(tidList.get(0), requestDto.getTxnType(), vfrom, vto));
//                        }
//                    }
//                }
//            }
//        } else if (searchTerm.isPresent()) {
//            logger.info("WITHOUT ACTION------------------------:");
//            if (tid.isPresent()) {
//                return updateWorkBook(bijlipayTransactionRepository.findAllTransactionDataWithSearch(tid.get(), searchTerm.get()));
//            }
//            return updateWorkBook(bijlipayTransactionRepository.findAllTransactionDataWithSearch(tidList.get(0), searchTerm.get()));
//        } else {
//            logger.info("WITHOUT ACTION------------------------:");
//            if (tid.isPresent()) {
//                return updateWorkBook(bijlipayTransactionRepository.findAllTransactionData(tid.get()));
//            }
//            return updateWorkBook(bijlipayTransactionRepository.findAllTransactionData(tidList.get(0)));
//        }
//        return updateWorkBook(bijlipayTransactionRepository.findAllTransactionData(tidList.get(0)));
//
//    }

    @Override
    public InputStreamResource downloadTransactionReport(Optional<Integer> action, List<String> tidList, Optional<String> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm) throws IOException, ParseException {
        List<Object[]> transactionsDtos = new ArrayList<>();
        if (action.isPresent()) {
            if (action.get() == TXN_RESPONSE_CODE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearch(tid.orElseGet(() -> tidList.get(0)), searchTerm.get()), tid, tidList, from, to);
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailureSearch(tid.orElseGet(() -> tidList.get(0)), searchTerm.get()), tid, tidList, from, to);
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch(tid.orElseGet(() -> tidList.get(0)), searchTerm.get()), tid, tidList, from, to);
                        }
                    } else {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccess(tid.orElseGet(() -> tidList.get(0))), tid, tidList, from, to);
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailure(tid.orElseGet(() -> tidList.get(0))), tid, tidList, from, to);
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailure(tid.orElseGet(() -> tidList.get(0))), tid, tidList, from, to);
                        }
                    }
                }
            } else if (action.get() == TXN_TYPE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (!requestDto.getTxnType().isEmpty()) {
                    if (searchTerm.isPresent()) {
                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearch(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), searchTerm.get()), tid, tidList, from, to);
                    } else {
                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnType(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                    }
                }
            } else if (action.get() == DATE_RANGE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getDateRange() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), vfrom, vto, searchTerm.get()), tid, tidList, from, to);
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithDateRange3(tid.orElseGet(() -> tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                        }
                    }
                }
            } else if (action.get() == ALL_FILTER) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ALL ACTION ------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto, searchTerm.get()), tid, tidList, from, to);
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                                if (requestDto.getTxnType().size() > 2) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("requestDto txnType all 3 filter search------- : {}", requestDto.getTxnType());
                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto, searchTerm.get()), tid, tidList, from, to);
                                    }
                                } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto, searchTerm.get()), tid, tidList, from, to);
                                }
                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto, searchTerm.get()), tid, tidList, from, to);
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto, searchTerm.get()), tid, tidList, from, to);
                            }
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                                if (requestDto.getTxnType().size() > 2) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("requestDto txnType all 3 filter without tid and search------- : {}", requestDto.getTxnType());
                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3848(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                                    }
                                } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
                                    return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                                }
                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                            }
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty()) {
                    logger.info(" ACTION txn status and txn type------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), searchTerm.get()), tid, tidList, from, to);
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            if (requestDto.getTxnType().size() > 2) {
                                requestDto.getTxnType().removeAll(List.of("38", "48"));
                                logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), searchTerm.get()), tid, tidList, from, to);
                            } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), searchTerm.get()), tid, tidList, from, to);
                            }
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), searchTerm.get()), tid, tidList, from, to);
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), searchTerm.get()), tid, tidList, from, to);
                        }
                    } else {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndType(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            if (requestDto.getTxnType().size() > 2) {
                                requestDto.getTxnType().removeAll(List.of("38", "48"));
                                logger.info("requestDto txnType any 2 filter without tid and search------- : {}", requestDto.getTxnType());
                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType3848(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                            } else if (requestDto.getTxnType().size() == 2 && new HashSet<>(requestDto.getTxnType()).contains("38,48")) {
                                return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                            }
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn status and date range------------------------: {}", action.get());
                    String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                    String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), vfrom, vto, searchTerm.get()), tid, tidList, from, to);
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), vfrom, vto, searchTerm.get()), tid, tidList, from, to);
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), vfrom, vto, searchTerm.get()), tid, tidList, from, to);
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3(tid.orElseGet(() -> tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3(tid.orElseGet(() -> tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3(tid.orElseGet(() -> tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                        }
                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType()),tid,tidList,from,to);
                    }
                }
            } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
                logger.info(" ACTION txn status and date range------------------------: {}", action.get());
                String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                if (searchTerm.isPresent()) {
                    if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), vfrom, vto, searchTerm.get()),tid,tidList,from,to);
                    } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), vfrom, vto, searchTerm.get()),tid,tidList,from,to);
                    } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), vfrom, vto, searchTerm.get()),tid,tidList,from,to);
                    }
                } else {
                    if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3(tid.orElseGet(() -> tidList.get(0)), vfrom, vto),tid,tidList,from,to);
                    } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3(tid.orElseGet(() -> tidList.get(0)), vfrom, vto),tid,tidList,from,to);
                    } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3(tid.orElseGet(() -> tidList.get(0)), vfrom, vto),tid,tidList,from,to);
                    }

                }
            } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                logger.info(" ACTION txn type and date range------------------------: {}", action.get());
                if (searchTerm.isPresent()) {
                    if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto, searchTerm.get()),tid,tidList,from,to);
                    }
                } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn type and date range------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto, searchTerm.get()), tid, tidList, from, to);
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return updateWorkBook(bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3(tid.orElseGet(() -> tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                        }
                    }
                }
            }
        } else if (searchTerm.isPresent()) {
            logger.info("WITHOUT ACTION------------------------:");
            return updateWorkBook(bijlipayTransactionRepository.findAllTransactionDataWithSearch(tid.orElseGet(() -> tidList.get(0)), searchTerm.get()), tid, tidList, from, to);
        } else {
            logger.info("WITHOUT ACTION------------------------:");
            return updateWorkBook(bijlipayTransactionRepository.findAllTransactionData(tid.orElseGet(() -> tidList.get(0))), tid, tidList, from, to);
        }
        return updateWorkBook(bijlipayTransactionRepository.findAllTransactionData(tid.orElseGet(() -> tidList.get(0))), tid, tidList, from, to);

    }

    private InputStreamResource updateWorkBook(List<Object[]> transactionsDtos, Optional<String> tid, List<String> tidList, Optional<Long> from, Optional<Long> to) throws IOException, ParseException {
        List<TransactionReportDto> result = new ArrayList<>();
        transactionsDtos.stream().forEach(m -> {
            TransactionReportDto txnDto = new TransactionReportDto();
            txnDto.setTxnDate(m[0] == null ? "NA" : m[0].toString());
            txnDto.setMid(m[1] == null ? "NA" : m[1].toString());
            txnDto.setPayeeVpaOrTid(m[2] == null ? "NA" : m[2].toString());           // tid
            txnDto.setResponseCodeOrTxnType(m[3] == null ? "NA" : m[3].toString());  // txnType
            txnDto.setBankRrn(m[4] == null ? "NA" : m[4].toString());
            if (Objects.equals(getTxnType(m[7].toString() + m[3].toString()), MTI_VOID_DESC)) {
                txnDto.setTxnAmount(m[6] == null ? "NA" : m[6].toString());
            } else {
                txnDto.setTxnAmount(m[5] == null ? "NA" : m[5].toString());
            }
            result.add(txnDto);
        });
        List<String> headers = Arrays.asList("TxnDate", "Mid", "Tid", "TxnType", "RRN", "TxnAmount");
        logger.info("Creating the excel file : ------------- txnSize : {}", result.size());

        String sheetName = "Pos Transactions";
        String fileName = "";
        if (from.isPresent() && to.isPresent()) {
            fileName = String.format(tid.orElseGet(() -> tidList.get(0)) + "_%s_%s_%s.xlsx", DateUtil.longtoDate(from.get(), "yyyMMdd"), DateUtil.longtoDate(to.get(), "yyyMMdd"), System.currentTimeMillis());
        } else {
            fileName = String.format(tid.orElseGet(() -> tidList.get(0)) + "_%s.xlsx", System.currentTimeMillis());
        }
        String filePath = fileUploadDir + "/" + fileName;
        InputStreamResource file = ReportUtils.posTxnExcelReport(result, headers, sheetName, filePath);

        UserTidVpaData userTidVpaData = userTidVpaDataRepository.findByTid(tid.orElseGet(() -> tidList.get(0)));

        SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
        settlementRequestResponse.setUser(userTidVpaData.getUser());
        if (from.isPresent() && to.isPresent()) {
            settlementRequestResponse.setFromDate(DateUtil.longtoDate(from.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setToDate(DateUtil.longtoDate(to.get(), "dd-MM-yyyy"));
        }
        settlementRequestResponse.setTid(tid.orElseGet(() -> tidList.get(0)));
        settlementRequestResponse.setMid(userTidVpaData.getMid());
        settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
        logger.info("after 3 month --------- : {}", DateUtil.addThreeMonthDate());
        settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
        settlementRequestResponse.setFileExist(true);
        settlementRequestResponse.setFileUrl(fileDownloadUrl + fileName);
        settlementRequestResponse.setFileName(fileName);
        settlementRequestResponse.setCategoryType(CATEGORY_TYPE_TRANSACTION);
        SettlementRequestResponse fileResponse = settlementRequestResponseRepository.save(settlementRequestResponse);
        logger.info("settlement request response final ----------: {}", fileResponse);

        return file;
//        return ReportUtils.transactionExcelReport(result, headers, SHEETNAME);
    }

    @Override
    public InputStreamResource downloadQrTransactionReport(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<String> vpa) throws ParseException {
        if (action.isPresent()) {
            switch (value.get()) {
                case DATE_RANGE_THIS_MONTH: {
                    return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxnThisMonthData(vpaDetails), vpa, vpaDetails, from, to);
                }
                case DATE_RANGE_LAST_30_DAYS: {
                    return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxnLast30DaysData(vpaDetails), vpa, vpaDetails, from, to);
                }
                case DATE_RANGE_CHOOSE_DATE: {
                    if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                        String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", vfrom, vto, vpaDetails.get(0), searchTerm.get());
                        return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTerm(vfrom, vto, vpa.orElseGet(() -> vpaDetails.get(0)), searchTerm.get()), vpa, vpaDetails, from, to);
                    } else if (from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", vfrom, vto, vpaDetails.get(0));
                        return updateQRWorkbook(staticQrTxnRepository.findAxisQrBasedOnVpaWithDates(vfrom, vto, vpa.orElseGet(() -> vpaDetails.get(0))), vpa, vpaDetails, from, to);
                    } else if (searchTerm.isPresent()) {
                        logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                        return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm(vpa.orElseGet(() -> vpaDetails.get(0)), searchTerm.get()), vpa, vpaDetails, from, to);
                    }
                }
            }
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxn(vpa.orElseGet(() -> vpaDetails.get(0))), vpa, vpaDetails, from, to);
        } else if (searchTerm.isPresent()) {
            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
            return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm(vpa.orElseGet(() -> vpaDetails.get(0)), searchTerm.get()), vpa, vpaDetails, from, to);
        } else {
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxn(vpaDetails), vpa, vpaDetails, from, to);
        }
    }

    @Override
    public InputStreamResource downloadQrTransactionReportData(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<String> vpa) throws ParseException {
        if (action.isPresent()) {
            switch (value.get()) {
                case DATE_RANGE_THIS_MONTH: {
                    return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxnThisMonthData(vpaDetails), vpa, vpaDetails, from, to);
                }
                case DATE_RANGE_LAST_30_DAYS: {
                    return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxnLast30DaysData(vpaDetails), vpa, vpaDetails, from, to);
                }
                case DATE_RANGE_CHOOSE_DATE: {
                    if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                        String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", vfrom, vto, vpaDetails.get(0), searchTerm.get());
                        return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTerm(vfrom, vto, vpa.orElseGet(() -> vpaDetails.get(0)), searchTerm.get()), vpa, vpaDetails, from, to);
                    } else if (from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", vfrom, vto, vpaDetails.get(0));
                        return updateQRWorkbook(staticQrTxnRepository.findAxisQrBasedOnVpaWithDates(vfrom, vto, vpa.orElseGet(() -> vpaDetails.get(0))), vpa, vpaDetails, from, to);
                    } else if (searchTerm.isPresent()) {
                        logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                        return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm(vpa.orElseGet(() -> vpaDetails.get(0)), searchTerm.get()), vpa, vpaDetails, from, to);
                    }
                }
            }
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxn(vpa.orElseGet(() -> vpaDetails.get(0))), vpa, vpaDetails, from, to);
        } else if (searchTerm.isPresent()) {
            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
            return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm(vpa.orElseGet(() -> vpaDetails.get(0)), searchTerm.get()), vpa, vpaDetails, from, to);
        } else {
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return updateQRWorkbook(staticQrTxnRepository.findAxisQrTxn(vpa.orElseGet(() -> vpaDetails.get(0))), vpa, vpaDetails, from, to);
        }
    }

    private InputStreamResource updateQRWorkbook(List<Object[]> txnList, Optional<String> vpa, List<String> vpaList, Optional<Long> from, Optional<Long> to) throws ParseException {
        List<AxisQrTransactionDto> result = new ArrayList<>();
        txnList.stream().forEach(m -> {
            AxisQrTransactionDto txnDto = new AxisQrTransactionDto();
            txnDto.setCustomerVpa(m[0] == null ? "NA" : m[0].toString());
            txnDto.setCreditVpa(m[1] == null ? "NA" : m[1].toString());
            txnDto.setMerchantId(m[2] == null ? "NA" : m[2].toString());
            txnDto.setMerchantChannelId(m[3] == null ? "NA" : m[3].toString());
            txnDto.setMerchantTransactionId(m[4] == null ? "NA" : m[4].toString());
            txnDto.setTransactionTimestamp(m[5] == null ? "NA" : m[5].toString());
            txnDto.setTransactionAmount(m[6] == null ? "NA" : m[6].toString());
            txnDto.setGatewayTransactionId(m[7] == null ? "NA" : m[7].toString());
            txnDto.setGatewayResponseCode(m[8] == null ? "NA" : m[8].toString());
            txnDto.setGatewayResponseMessage(m[9] == null ? "NA" : m[9].toString());
            txnDto.setRrn(m[10] == null ? "NA" : m[10].toString());
            txnDto.setChecksum(m[11] == null ? "NA" : m[11].toString());
            txnDto.setPayeractype(m[12] == null ? "NA" : m[12].toString());
            txnDto.setPurposeCode(m[13] == null ? "NA" : m[13].toString());
            txnDto.setInitMode(m[14] == null ? "NA" : m[14].toString());
            result.add(txnDto);
        });
        List<String> headers = Arrays.asList("customerVpa", "creditVpa", "merchantId", "merchantChannelId", "merchantTransactionId", "transactionTimestamp", "transactionAmount", "gatewayTransactionId", "gatewayResponseCode", "gatewayResponseMessage", "rrn", "checksum", "payeractype", "purposeCode", "initMode");
        String sheetName = "Qr Transaction Data";
        logger.info("Creating the excel file : ------------- Qr txnSize : {}", result.size());
        String fileName = "";
        if (from.isPresent() && to.isPresent()){
            fileName = String.format("QrTransactionReport_" + "%s_%s_%s.xlsx", DateUtil.longtoDate(from.get(), "yyyMMdd"), DateUtil.longtoDate(to.get(), "yyyMMdd"),System.currentTimeMillis());
        }else {
            fileName = String.format("QrTransactionReport_" + "%s.xlsx",System.currentTimeMillis());
        }
        String filePath = fileUploadDir +"/"+ fileName;
        InputStreamResource file =ReportUtils.QrTxnReport(result, headers, sheetName, filePath);

//        SXSSFWorkbook sxssfWorkbook = ReportUtils.QrTransactionExcelReport(result, headers, sheetName);

//        List<String> headers = List.of("Customer VPA", "Credit VPA", "Merchant ID", "Transaction ID", "Amount");
//        String sheetName = "QR Transactions";
//        String fileName = String.format("QrTransactionReport_" + "%s_%s_%s.xlsx", DateUtil.longtoDate(from.get(), "yyyMMdd"), DateUtil.longtoDate(to.get(), "yyyMMdd"),System.currentTimeMillis());
//        String filePath = fileUploadDir +"/"+ fileName;
//        SXSSFWorkbook sxssfWorkbook =ReportUtils.QrTxnReport(result, headers, sheetName, filePath);

        SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
        settlementRequestResponse.setUser(userRepository.findByUser(getSessionUser().getId()));
        if(from.isPresent() && to.isPresent()){
            settlementRequestResponse.setFromDate(DateUtil.longtoDate(from.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setToDate(DateUtil.longtoDate(to.get(), "dd-MM-yyyy"));
        }
        settlementRequestResponse.setVpa(vpa.orElseGet(()->vpaList.get(0)));
        settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
        logger.info("after 3 month --------- : {}", DateUtil.addThreeMonthDate());
        settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
        settlementRequestResponse.setFileExist(true);
        settlementRequestResponse.setFileUrl(fileDownloadUrl + fileName);
        settlementRequestResponse.setFileName(fileName);
        settlementRequestResponse.setCategoryType(CATEGORY_TYPE_TRANSACTION);
        SettlementRequestResponse fileResponse = settlementRequestResponseRepository.save(settlementRequestResponse);
        logger.info("settlement request response final ----------: {}", fileResponse);
        return file;
    }

    @Override
    public void requestMailForInvoiceCreation(Invoice resqDto) throws MessagingException {
        Invoice invoice1 = new Invoice();
        invoice1.setInvoiceDate(resqDto.getInvoiceDate());
        invoice1.setInvoiceType(resqDto.getType() == 1 ? "MDR" : "RENTAL");
        invoice1.setEmail(resqDto.getEmail());
        invoice1.setTid(resqDto.getTid());
        Invoice save = invoiceRepository.save(invoice1);
        ForgotPasswordEmail invoiceMailRequest = new ForgotPasswordEmail();
        String mailTemplate = "Hi Team, \n\nGenerate Invoice For the Mid - %s and Tid - %s Month - %s,  \n\n" + "send it to the following mail id - %s \n\n" + "Regards,\nTeam Bijlipay \n\n" + "Note : This is an auto generated email. Please do not respond to this email id.";
        invoiceMailRequest.setSubject(String.format("%s invoice Request", save.getInvoiceType()));
        invoiceMailRequest.setBody(String.format(mailTemplate, userTidVpaDataRepository.findByTid(save.getTid()).getMid(), save.getTid(), save.getInvoiceDate(), save.getEmail()));
//        paperRollRequestMail.setReceiverEmail(save.getEmail());
        invoiceMailRequest.setReceiverEmail(invoiceRecMail);
        mailHandler.commonMailRequestWithoutAttachment(invoiceMailRequest);
    }

    @Override
    @Async("asyncExecutor")
//    @Transactional
    public void pushTxnNotifications(SwitchTransactionsDto switchTransactionsDto) {
        Gson gson = new Gson();
        String requestData = gson.toJson(switchTransactionsDto);
        try {
            UserTidVpaData userTidVpaData = userTidVpaDataRepository.findByTid(switchTransactionsDto.getTerminalId());
            if (userTidVpaData == null) {
            } else {
                if (switchTransactionsDto.getMti().equals("0200") || ((switchTransactionsDto.getTxnType().equals("37") || switchTransactionsDto.getTxnType().equals("39")) && switchTransactionsDto.getMti().equalsIgnoreCase("0100"))) {
                    if (switchTransactionsDto.getTxnResponseCode().equals("00")) {
                        User cUser = userRepository.getOne(userTidVpaData.getUser().getId());
                        String txnType = getTxnType(switchTransactionsDto.getMti() + switchTransactionsDto.getTxnType());
                        if (txnType == MTI_SETTLEMENT) { // To Skip the settlement Transaction notifications.
                            return;
                        }
                        if (switchTransactionsDto.getMti().equals("0320")) { // Since not sure about processing code, using just MTI
                            txnType = MTI_BATCH_UPLOAD;
                        } else if (switchTransactionsDto.getMti().equals("0400")) {
                            txnType = MTI_REVERSAL_DESC;
                        }
                        Category category = new Category();
                        category.setCategoryType(TRANSACTION_ID);
                        if (txnType == MTI_VOID_DESC) {
                            category.setDescription("Transaction Amount of INR " + "-" + switchTransactionsDto.getTxnAdditionalAmount() + " successful in TID: " + switchTransactionsDto.getTerminalId());
                        } else {
                            category.setDescription("Transaction Amount of INR " + switchTransactionsDto.getTxnAmount() + " successful in TID: " + switchTransactionsDto.getTerminalId());
                        }
                        category.setUserTidVpaData(userTidVpaData);
                        category.setUser(cUser);
                        Category finalResponse = categoryRepository.save(category);
                        String redirectTo = commonType(category.getCategoryType());
                        sendPushNotificationGenerate(txnType, finalResponse.getDescription(), finalResponse, redirectTo, cUser);
                    } else {
                    }
                } else {

                }
            }
        } catch (Exception e) {
            logger.error("PUSHTXNNOTIFICATIONS : Exception is push settlement notification : {}", e.getMessage());
        }
    }


    @Override
    @Async("asyncExecutor")
//    @Transactional
    public void pushSettlementNotifications(SettlementNotificationDto settlementNotificationDtos) {
        logger.info("Push notification is called : ------ : {}", settlementNotificationDtos);
        if (settlementNotificationDtos == null) {
        } else {
            String Mid = settlementNotificationDtos.getMid();
            if (Mid != null) {
                try {
                    logger.info("Notification Data Creation Started : ---- : {}", Mid);
                    List<UserTidVpaData> userTidVpaDataList = userTidVpaDataRepository.findByMid(Mid);
                    if (userTidVpaDataList == null || userTidVpaDataList.isEmpty() || userTidVpaDataList.size() == 0) {
                        logger.info("The mid is not signed up with Merchant application : ------- : {}", Mid);
                    } else {
                        UserTidVpaData userTidVpaData = userTidVpaDataList.get(0);
                        logger.info("Merchant details : ---- : {}", userTidVpaData);
                        User cUser = userRepository.getOne(userTidVpaData.getUser().getId());
                        logger.info("User details : ---- : {}", cUser);

                        String fileUrl = fileStorageService.convertUrlToPdf(settlementNotificationDtos.getSettlementFileUrl());

                        Category finalResponse = saveCategoryAndSettlementData(userTidVpaData, cUser, settlementNotificationDtos, fileUrl);

//                        Category category = new Category();
//                        category.setCategoryType(SETTLEMENT_ID);
////                        category.setDescription("Settlement for today is completed for " + userTidVpaData.getMid());
//                        category.setDescription("Settlement report generation is completed for " + userTidVpaData.getMid());
//                        category.setUserTidVpaData(userTidVpaData);
//                        category.setUser(cUser);
//                        category.setSettlementFileUrl(settlementNotificationDtos.getSettlementFileUrl());
//                        category.setFileUrl(fileUrl);
//                        logger.info("Before category save : ------------ : {}", category);
//                        Category finalResponse = categoryRepository.save(category);
//                        logger.info("Category saved : -------------- : {}", finalResponse);
//
//                        SettlementRequestResponse settlementRequestResponse = settlementRequestResponseRepository.findByUniqueId(settlementNotificationDtos.getUniqueId());
//                        if (settlementRequestResponse !=null){
//                            settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
//                            logger.info("after 3 month --------- : {}",DateUtil.addThreeMonthDate());
//                            settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
//                            settlementRequestResponse.setFileExist(true);
//                            settlementRequestResponse.setSettlementS3Url(settlementNotificationDtos.getSettlementFileUrl());
//                            settlementRequestResponse.setFileUrl(fileUrl);
//                            SettlementRequestResponse fileResponse = settlementRequestResponseRepository.save(settlementRequestResponse);
//                            logger.info("settlement request response final ----------: {}",fileResponse);
//                        }

                        String redirectTo = commonType(finalResponse.getCategoryType());
                        logger.info("Notification data push : title       : ---- : {}", userTidVpaData.getMid() + "_" + redirectTo);
                        logger.info("Notification data push : User        : ---- : {}", cUser.getEmail());
                        logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                        logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                        logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                        sendPushNotificationGenerate(userTidVpaData.getMid() + "_" + redirectTo, finalResponse.getDescription(), finalResponse, redirectTo, cUser);
                        logger.info("Notification Data Creation Completed : ---- : {}", Mid);
                    }

                } catch (Exception e) {
                    logger.error("Exception is push settlement notification : {}", e.getMessage());
                }
            }
        }
    }
    @Transactional
    public Category saveCategoryAndSettlementData(UserTidVpaData userTidVpaData, User cUser, SettlementNotificationDto settlementNotificationDtos, String fileUrl) {
        Category category = new Category();
        category.setCategoryType(SETTLEMENT_ID);
//                        category.setDescription("Settlement for today is completed for " + userTidVpaData.getMid());
        category.setDescription("Settlement report generation is completed for " + userTidVpaData.getMid());
        category.setUserTidVpaData(userTidVpaData);
        category.setUser(cUser);
        category.setSettlementFileUrl(settlementNotificationDtos.getSettlementFileUrl());
        category.setFileUrl(fileUrl);
        logger.info("Before category save : ------------ : {}", category);
        Category finalResponse = categoryRepository.save(category);
        logger.info("Category saved : -------------- : {}", finalResponse);

        SettlementRequestResponse settlementRequestResponse = settlementRequestResponseRepository.findByUniqueId(settlementNotificationDtos.getUniqueId());
        if (settlementRequestResponse !=null){
            settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
            logger.info("after 3 month --------- : {}",DateUtil.addThreeMonthDate());
            settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
            settlementRequestResponse.setFileExist(true);
            settlementRequestResponse.setSettlementS3Url(settlementNotificationDtos.getSettlementFileUrl());
            settlementRequestResponse.setFileUrl(fileUrl);
            SettlementRequestResponse fileResponse = settlementRequestResponseRepository.save(settlementRequestResponse);
            logger.info("settlement request response final ----------: {}",fileResponse);
        }

        return finalResponse;
    }


    @Override
    public void pushQRSettlementNotifications(SettlementNotificationDto requestDto) {
        if (requestDto != null) {
            if (requestDto.getVpa() != null) {
                try {
                    logger.info("Notification Data Creation Started : ---- : {}", requestDto.getVpa());
                    List<UserTidVpaData> userTidVpaDataList = userTidVpaDataRepository.findVpa(requestDto.getVpa());
                    if (userTidVpaDataList == null || userTidVpaDataList.isEmpty() || userTidVpaDataList.size() == 0) {
                        logger.info("The mid is not signed up with Merchant application : ------- : {}", requestDto.getVpa());
                    } else {
                        UserTidVpaData userTidVpaData = userTidVpaDataList.get(0);
                        logger.info("Merchant details : ---- : {}", userTidVpaData);
                        User cUser = userRepository.getOne(userTidVpaData.getUser().getId());
                        logger.info("User details : ---- : {}", cUser);

                        String fileUrl = fileStorageService.convertUrlToPdf(requestDto.getSettlementFileUrl());


                        Category category = new Category();
                        category.setCategoryType(SETTLEMENT_ID);
                        category.setDescription("Settlement report generation is completed for " + userTidVpaData.getVpa());
                        category.setUserTidVpaData(userTidVpaData);
                        category.setUser(cUser);
                        category.setSettlementFileUrl(requestDto.getSettlementFileUrl());
                        category.setFileUrl(fileUrl);
                        logger.info("Before category save : ------------ : {}", category);
                        Category finalResponse = categoryRepository.save(category);
                        logger.info("Category saved : -------------- : {}", finalResponse);

                        SettlementRequestResponse settlementRequestResponse = settlementRequestResponseRepository.findByUniqueId(requestDto.getUniqueId());
                        if (settlementRequestResponse !=null){
                            settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
                            logger.info("after 3 month --------- : {}",DateUtil.addThreeMonthDate());
                            settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
                            settlementRequestResponse.setFileExist(true);
                            settlementRequestResponse.setSettlementS3Url(requestDto.getSettlementFileUrl());
                            settlementRequestResponse.setFileUrl(fileUrl);
                            SettlementRequestResponse fileResponse = settlementRequestResponseRepository.save(settlementRequestResponse);
                            logger.info("settlement request response final ----------: {}",fileResponse);
                        }

                        String redirectTo = commonType(category.getCategoryType());
                        logger.info("Notification data push : title       : ---- : {}", userTidVpaData.getMid() + "_" + redirectTo);
                        logger.info("Notification data push : User        : ---- : {}", cUser.getEmail());
                        logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                        logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                        logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                        sendPushNotificationGenerate(userTidVpaData.getMid() + "_" + redirectTo, finalResponse.getDescription(), finalResponse, redirectTo, cUser);
                        logger.info("Notification Data Creation Completed : ---- : {}", requestDto.getVpa());
                    }

                } catch (Exception e) {
                    logger.error("Exception is push settlement notification : {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public void insertStaticQrTxnData(AxisQrTransactionDto request) throws ParseException {
        Gson gson = new Gson();
        String requestPayload = gson.toJson(request);
        logger.info("Static Qr Txn Request Data ---{}", requestPayload);
        StaticQrTxn check = staticQrTxnRepository.findByAxisQrTxnBasedOnRRN(request.getRrn());
        if (check == null) {
            logger.info("enter to insert static qr txn data --{}", request.getRrn());
            StaticQrTxn staticQrTxn1 = new StaticQrTxn();
            staticQrTxn1.setCustomerVpa(request.getCustomerVpa());
            staticQrTxn1.setCreditVpa(request.getCreditVpa());
            staticQrTxn1.setMerchantId(request.getMerchantId());
            staticQrTxn1.setMerchantChannelId(request.getMerchantChannelId());
            staticQrTxn1.setMerchantTransactionId(request.getMerchantTransactionId());
            if (request.getCreditVpa().contains("kvb")) {
//                logger.info("request Date ----{}", request.getTransactionTimestamp());
                DateFormat parseFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                String sDate = request.getTransactionTimestamp();
                Date date1 = parseFormat.parse(sDate);
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                sDate = displayFormat.format(date1);
                Date transDate = displayFormat.parse(sDate);
//                logger.info("TransactionDate --{}", transDate);
                staticQrTxn1.setCreatedAt(transDate);
                String kvbDate = DateUtil.dateToString(transDate);
                String date = DateUtil.kvbDateFormat(kvbDate);
//                logger.info("Formatted Date KVB --{}",date);
                staticQrTxn1.setTransactionTimestamp(date);
                if (request.getGatewayResponseCode().equals("0")) {
                    staticQrTxn1.setGatewayResponseCode("00");
                } else {
                    staticQrTxn1.setGatewayResponseCode(request.getGatewayResponseCode());
                }
            } else {
                String txnDateInput = request.getTransactionTimestamp().replace(" ", "T");
                String txnDateInput1 = txnDateInput.replace(".", ":");
                String txnDate1 = DateUtil.stringToDateFroAxis(txnDateInput1);
                Date outDate = DateUtil.stringToDate(txnDate1);
                staticQrTxn1.setTransactionTimestamp(txnDateInput);
                staticQrTxn1.setCreatedAt(outDate);
                staticQrTxn1.setGatewayResponseCode(request.getGatewayResponseCode());
            }
            staticQrTxn1.setTransactionAmount(Float.parseFloat(request.getTransactionAmount()));
            staticQrTxn1.setGatewayTransactionId(request.getGatewayTransactionId());
            staticQrTxn1.setGatewayResponseMessage(request.getGatewayResponseMessage());
            staticQrTxn1.setRrn(request.getRrn());
            staticQrTxn1.setChecksum(request.getChecksum());
            staticQrTxn1.setPayeractype(request.getPayeractype());
            staticQrTxn1.setPurposeCode(request.getPurposeCode());
            staticQrTxn1.setInitMode(request.getInitMode());
            staticQrTxnRepository.save(staticQrTxn1);
        }
    }

    @Override
    @Async("asyncExecutor")
//    @Transactional
    public void pushTxnNotificationsForAxisQr(AxisQrTransactionDto axisQrTransactionDto) {
        try {
            if (axisQrTransactionDto.getGatewayResponseCode().equals("00")) {
                UserTidVpaData qrVpaDetails = userTidVpaDataRepository.findByVpa(axisQrTransactionDto.getCreditVpa());
                User cUser = userRepository.findByUser(qrVpaDetails.getUser().getId());
                String TxnType = "QR Transaction";
                String Description = "QR Transaction Amount of INR " + axisQrTransactionDto.getTransactionAmount() + " successful in Qr vpa: " + axisQrTransactionDto.getCreditVpa();
                String redirectTo = PUSH_NOTIFICATION_REDIRECT_TO_TRANSACTION;
                sendPushNotificationGenerateForAxisQr(TxnType, Description, qrVpaDetails, redirectTo, cUser);
            }
        } catch (Exception e) {
            logger.error("PUSHTXNNOTIFICATIONS : Exception is push axis qr transaction notification : {}", e.getMessage());
        }
    }

    @Async("asyncExecutor")
    private void sendPushNotificationGenerateForAxisQr(String title, String reason, UserTidVpaData userTidVpaData, String redirectTo, User userToNotify) {
        Gson gson = new Gson();
        PushNotificationResponseDto<NotificationBodyDtoForQr> pushNotificationResponseDto = new PushNotificationResponseDto<>();
        pushNotificationResponseDto.setNotification(new NotificationDto(title, reason));
        NotificationBodyDtoForQr qrLeadDto = new NotificationBodyDtoForQr();
        qrLeadDto.setRedirectTo(redirectTo);
        qrLeadDto.setVpaId(userTidVpaData.getId());
        qrLeadDto.setVpa(userTidVpaData.getVpa());
        qrLeadDto.setMessage(reason);
        pushNotificationResponseDto.setToken(userToNotify != null ? userToNotify.getDeviceToken() : null);
        Notification notification = new Notification();
        notification.setNotificationTitle(title);
        notification.setNotificationReason(reason);
        notification.setNotificationRedirectTo(redirectTo);
        notification.setUser(userToNotify);
        notification.setData(new Gson().toJson(qrLeadDto));
        Notification afterNotificationSave = notificationRepository.save(notification);
        qrLeadDto.setNotificationId(afterNotificationSave.getId());
        String pushData = gson.toJson(qrLeadDto);
        Map<String, String> dataElement = new HashMap<>();
        dataElement.put("keysandvalues", pushData);
        pushNotificationResponseDto.setData(dataElement);
        try {
            Map<String, PushNotificationResponseDto> result = new HashMap<>();
            result.put("message", pushNotificationResponseDto);
            String payload = gson.toJson(result);
            String firebaseResponse = this.sendPushNotification(payload);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean insertAxisMissingData(String missingDataFile) {
        boolean updated = false;
        // get the csv file
        List<StaticQrTxn> qrListOriginal = new ArrayList<>();

        String fileExtension = "";
        int index = missingDataFile.lastIndexOf(".");
        if (index > 0) {
            fileExtension = missingDataFile.substring(index + 1);
            if (fileExtension.equals("csv")) {
                BufferedReader fileReader = null;
                try {
                    String line = "";
                    fileReader = new BufferedReader(new FileReader(missingDataFile));
                    Files.write(Paths.get(missingDataFile), new String(Files.readAllBytes(Paths.get(missingDataFile))).replace("\"", "").getBytes());
                    //read csv header
                    fileReader.readLine();
                    int lineNumber = 0;
                    // Read customer data line by line
                    while ((line = fileReader.readLine()) != null) {
                        lineNumber++;
                        String[] element = line.split(",");
                        if (element.length > 0 && lineNumber != 0) {
                            logger.info("Enter to insert Axis Missing Data--{}", element[10]);
                            StaticQrTxn rrnCheck = staticQrTxnRepository.findByAxisQrTxnBasedOnRRN(element[10]);
                            if (rrnCheck == null) {
                                StaticQrTxn staticQrTxn = new StaticQrTxn();
                                staticQrTxn.setCustomerVpa(element[0] != null ? element[0] : "");
                                staticQrTxn.setCreditVpa(element[1] != null ? element[1] : "");
                                staticQrTxn.setMerchantId(element[2] != null ? element[2] : "");
                                staticQrTxn.setMerchantChannelId(element[3] != null ? element[3] : "");
                                staticQrTxn.setMerchantTransactionId(element[4] != null ? element[4] : "");
                                String txnDateInput = element[5].replace(" ", "T");
//                                logger.info("Txn Date Before--{}",txnDateInput);
                                String txnDate1 = DateUtil.stringToDateFroAxis(txnDateInput);
//                                logger.info("Txn Date After--{}",txnDate1);
                                Date outDate = DateUtil.stringToDate(txnDate1);
                                staticQrTxn.setTransactionTimestamp(txnDateInput);
                                staticQrTxn.setCreatedAt(outDate);
                                staticQrTxn.setTransactionAmount(Float.parseFloat(element[6]));
                                staticQrTxn.setGatewayTransactionId(element[7] != null ? element[7] : "");
                                staticQrTxn.setGatewayResponseCode(element[8] != null ? element[8] : "");
                                staticQrTxn.setGatewayResponseMessage(element[9] != null ? element[9] : "");
                                staticQrTxn.setRrn(element[10] != null ? element[10] : "");
                                staticQrTxn.setChecksum(element[11] != null ? element[11] : "");
                                staticQrTxn.setPayeractype(element[12] != null ? element[12] : "");
                                staticQrTxn.setPurposeCode(element[13] != null ? element[13] : "");
                                staticQrTxn.setInitMode(element[14] != null ? element[14] : "");
                                qrListOriginal.add(staticQrTxn);
                            }
                        }
                    }
                    fileReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    staticQrTxnRepository.saveAll(qrListOriginal);
                    updated = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                File file = new File(missingDataFile);
                file.delete();
                logger.info("Axis Missing Data File Removed Successfully");

                if (qrListOriginal.size() > 0) {
                    qrListOriginal.forEach(l -> {
                        AxisQrTransactionDto dto = new AxisQrTransactionDto();
                        dto.setCreditVpa(l.getCreditVpa());
                        dto.setTransactionAmount(String.valueOf(l.getTransactionAmount()));
                        dto.setGatewayResponseCode(l.getGatewayResponseCode());
                        dto.setRrn(l.getRrn());
                        this.pushTxnNotificationsForAxisQr(dto);
                    });
                }
            }
        }
        return updated;
    }

    @Override
    public void downloadTxnReport(Optional<Long> from, Optional<Long> to, Optional<String> tid, UserTidVpaData userTidVpaData) throws ParseException, IOException {
        if (from.isPresent() && to.isPresent() && tid.isPresent()) {
            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
            List<Object[]> txnData = bijlipayTransactionRepository.fetchTidBasedTxnData(vfrom,vto,tid.get());
            List<TransactionReportDto> result = new ArrayList<>();
            txnData.stream().forEach(m -> {
                TransactionReportDto txnDto = new TransactionReportDto();
                txnDto.setTxnDate(m[0] == null ? "NA" : m[0].toString());
                txnDto.setMid(m[1] == null ? "NA" : m[1].toString());
                txnDto.setPayeeVpaOrTid(m[2] == null ? "NA" : m[2].toString());           // tid
                txnDto.setResponseCodeOrTxnType(m[3] == null ? "NA" : m[3].toString());  // txnType
                txnDto.setBankRrn(m[4] == null ? "NA" : m[4].toString());
                if (Objects.equals(getTxnType(m[7].toString() + m[3].toString()), MTI_VOID_DESC)) {
                    txnDto.setTxnAmount(m[6] == null ? "NA" : m[6].toString());
                } else {
                    txnDto.setTxnAmount(m[5] == null ? "NA" : m[5].toString());
                }
                result.add(txnDto);
            });
            List<String> headers = Arrays.asList("TxnDate", "Mid", "Tid", "TxnType", "RRN", "TxnAmount");
            String sheetName = "Pos Transactions";
            String fileName = String.format(tid.get() + "_%s_%s_%s.xlsx", DateUtil.longtoDate(from.get(), "yyyMMdd"), DateUtil.longtoDate(to.get(), "yyyMMdd"),System.currentTimeMillis());
            String filePath = fileUploadDir +"/"+ fileName;
            ReportUtils.posTxnExcelReport(result, headers, sheetName, filePath);

            SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
            settlementRequestResponse.setUser(userTidVpaData.getUser());
            settlementRequestResponse.setFromDate(DateUtil.longtoDate(from.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setToDate(DateUtil.longtoDate(to.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setTid(tid.get());
            settlementRequestResponse.setMid(userTidVpaData.getMid());
            settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
            logger.info("after 3 month --------- : {}", DateUtil.addThreeMonthDate());
            settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
            settlementRequestResponse.setFileExist(true);
            settlementRequestResponse.setFileUrl(fileDownloadUrl + fileName);
            settlementRequestResponse.setCategoryType(CATEGORY_TYPE_TRANSACTION);
            SettlementRequestResponse fileResponse = settlementRequestResponseRepository.save(settlementRequestResponse);
            logger.info("settlement request response final ----------: {}", fileResponse);
        }
    }

    @Override
    public void downloadQrReport(Optional<Long> from, Optional<Long> to, Optional<String> vpa) throws ParseException {
        if (from.isPresent() && to.isPresent()) {
            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
            List<Object[]> qrTxnData = staticQrTxnRepository.fetchTidBasedTxnData(vfrom,vto,vpa.get());
            List<AxisQrTransactionDto> result = new ArrayList<>();
            qrTxnData.stream().forEach(m -> {
                AxisQrTransactionDto txnDto = new AxisQrTransactionDto();
                txnDto.setCustomerVpa(m[0] == null ? "NA" : m[0].toString());
                txnDto.setCreditVpa(m[1] == null ? "NA" : m[1].toString());
                txnDto.setMerchantId(m[2] == null ? "NA" : m[2].toString());
                txnDto.setMerchantChannelId(m[3] == null ? "NA" : m[3].toString());
                txnDto.setMerchantTransactionId(m[4] == null ? "NA" : m[4].toString());
                txnDto.setTransactionTimestamp(m[5] == null ? "NA" : m[5].toString());
                txnDto.setTransactionAmount(m[6] == null ? "NA" : m[6].toString());
                txnDto.setGatewayTransactionId(m[7] == null ? "NA" : m[7].toString());
                txnDto.setGatewayResponseCode(m[8] == null ? "NA" : m[8].toString());
                txnDto.setGatewayResponseMessage(m[9] == null ? "NA" : m[9].toString());
                txnDto.setRrn(m[10] == null ? "NA" : m[10].toString());
                txnDto.setChecksum(m[11] == null ? "NA" : m[11].toString());
                txnDto.setPayeractype(m[12] == null ? "NA" : m[12].toString());
                txnDto.setPurposeCode(m[13] == null ? "NA" : m[13].toString());
                txnDto.setInitMode(m[14] == null ? "NA" : m[14].toString());
                result.add(txnDto);
            });
            List<String> headers = List.of("Customer VPA", "Credit VPA", "Merchant ID", "Transaction ID", "Amount");
            String sheetName = "QR Transactions";
            String fileName = String.format("QrTransactionReport_" + "%s_%s_%s.xlsx", DateUtil.longtoDate(from.get(), "yyyMMdd"), DateUtil.longtoDate(to.get(), "yyyMMdd"),System.currentTimeMillis());
            String filePath = fileUploadDir +"/"+ fileName;
            InputStreamResource sxssfWorkbook =ReportUtils.QrTxnReport(result, headers, sheetName, filePath);

            SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
            settlementRequestResponse.setUser(userRepository.findByUser(getSessionUser().getId()));
            settlementRequestResponse.setFromDate(DateUtil.longtoDate(from.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setToDate(DateUtil.longtoDate(to.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setVpa(vpa.get());
            settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
            logger.info("after 3 month --------- : {}", DateUtil.addThreeMonthDate());
            settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
            settlementRequestResponse.setFileExist(true);
            settlementRequestResponse.setFileUrl(fileDownloadUrl + fileName);
            settlementRequestResponse.setCategoryType(CATEGORY_TYPE_TRANSACTION);
            SettlementRequestResponse fileResponse = settlementRequestResponseRepository.save(settlementRequestResponse);
            logger.info("settlement request response final ----------: {}", fileResponse);

        }
    }

    public static void QrTransactionExcelReport(List<AxisQrTransactionDto> result, List<String> headers, String sheetName, String filePath) {
        SXSSFWorkbook wb = new SXSSFWorkbook(); // Initialize workbook
        Sheet sh = wb.createSheet(sheetName); // Create sheet

        Row headerRow = sh.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            headerRow.createCell(i).setCellValue(headers.get(i));
        }
        int startRow = 1;
        for (AxisQrTransactionDto transaction : result) {
            Row shRow = sh.createRow(startRow++);
            List<String> rowAsString = transaction.getAsList(); // Convert DTO to list
            for (int cellnum = 0; cellnum < rowAsString.size(); cellnum++) {
                shRow.createCell(cellnum).setCellValue(rowAsString.get(cellnum));
            }
        }
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            wb.write(fileOut);
            wb.close();
            System.out.println("Excel file written successfully to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing Excel file: " + e.getMessage());
        }
    }

    public static void posTransactionExcelReport(List<TransactionReportDto> result, List<String> headers, String sheetName, String filePath) {
        SXSSFWorkbook wb = new SXSSFWorkbook(); // Initialize workbook
        Sheet sh = wb.createSheet(sheetName); // Create sheet

        Row headerRow = sh.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            headerRow.createCell(i).setCellValue(headers.get(i));
        }
        int startRow = 1;
        for (TransactionReportDto transaction : result) {
            Row shRow = sh.createRow(startRow++);
            List<String> rowAsString = transaction.getAsList(); // Convert DTO to list
            for (int cellnum = 0; cellnum < rowAsString.size(); cellnum++) {
                shRow.createCell(cellnum).setCellValue(rowAsString.get(cellnum));
            }
        }
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            wb.write(fileOut);
            wb.close();
            System.out.println("Excel file written successfully to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing Excel file: " + e.getMessage());
        }
    }

    @Override
    @Async("asyncExecutor")
//    @Transactional
    public void pushSettlementNotificationsForToday(SettlementNotificationUpdate settlementNotificationDtos) {
        logger.info("Push notification is called : ------ : {}", settlementNotificationDtos);
        if (settlementNotificationDtos == null) {
        } else {
            String[] Mid = settlementNotificationDtos.getMid();
            if (Mid.length > 0) {
                try {
                    for (String mid : Mid) {
                        logger.info("Notification Data Creation Started : ---- : {}", mid);
                        List<UserTidVpaData> merchantDeatilsList = userTidVpaDataRepository.findByMid(mid);
                        if (merchantDeatilsList == null || merchantDeatilsList.isEmpty() || merchantDeatilsList.size() == 0) {
                            logger.info("The mid is not signed up with Merchant application : ------- : {}", mid);
                        } else {
                            UserTidVpaData merchantDetails = merchantDeatilsList.get(0);
                            logger.info("Merchant details : ---- : {}", merchantDetails);
                            User cUser = userRepository.getOne(merchantDetails.getUser().getId());
                            logger.info("User details : ---- : {}", cUser);
                            Category category = new Category();
                            category.setCategoryType(SETTLEMENT_ID);
                            category.setDescription("Settlement for today is completed for " + merchantDetails.getMid());
                            category.setUserTidVpaData(merchantDetails);
                            category.setUser(cUser);
                            logger.info("Before category save : ------------ : {}", category);
                            Category finalResponse = categoryRepository.save(category);
                            logger.info("Category saved : -------------- : {}", finalResponse);
                            String redirectTo = commonType(category.getCategoryType());
                            logger.info("Notification data push : title       : ---- : {}", merchantDetails.getMid() + "_" + redirectTo);
                            logger.info("Notification data push : User        : ---- : {}", cUser.getEmail());
                            logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                            logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                            logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                            sendPushNotificationGenerate(merchantDetails.getMid() + "_" + redirectTo, finalResponse.getDescription(), finalResponse, redirectTo, cUser);
                            logger.info("Notification Data Creation Completed : ---- : {}", mid);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception is push settlement notification : {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public void insertAnteraPaytivoTxn(List<Object[]> txnData) {
        logger.info("Antera Paytivo txn data Size --{}", txnData.size());
        List<BijlipayTransaction> transactions = new ArrayList<>();
        if (txnData.size() > 0) {
            txnData.forEach(o -> {
                int count = 0;
                BijlipayTransaction bijlipayTransaction = new BijlipayTransaction();

                bijlipayTransaction.setTxnAdditionalAmount(String.valueOf(o[count++]));
                bijlipayTransaction.setAuthCode(String.valueOf(o[count++]));
                bijlipayTransaction.setBatchNumber(String.valueOf(o[count++]));
                bijlipayTransaction.setInvoiceNumber(String.valueOf(o[count++]));
                bijlipayTransaction.setAuthCode(String.valueOf(o[count++]));
                bijlipayTransaction.setMaskedcardnumber(String.valueOf(o[count++]));
                bijlipayTransaction.setMerchantId(String.valueOf(o[count++]));
                bijlipayTransaction.setMti(String.valueOf(o[count++]));
                bijlipayTransaction.setPanEntryMode(String.valueOf(o[count++]));
                bijlipayTransaction.setrRNumber(String.valueOf(o[count++]));
                Date tDate = DateUtil.convertStringToDate(String.valueOf(o[count++]), "yyyy-MM-dd HH:mm:ss");
                bijlipayTransaction.setResponseReceivedTime(tDate);
                bijlipayTransaction.setStan(String.valueOf(o[count++]));
                bijlipayTransaction.setTerminalId(String.valueOf(o[count++]));
                if (o[count] != null) {
                    bijlipayTransaction.setTxnAmount(Float.parseFloat(String.valueOf(o[count++])));
                } else {
                    bijlipayTransaction.setTxnAmount(0.0);
                    count++;
                }
                bijlipayTransaction.setTxnCorrelationId(String.valueOf(o[count++]));
                bijlipayTransaction.setTxnDate(String.valueOf(o[count++]));
                bijlipayTransaction.setTxnResponseCode(String.valueOf(o[count++]));
                bijlipayTransaction.setTxnTime(String.valueOf(o[count++]));
                bijlipayTransaction.setTxnType(String.valueOf(o[count++]));
                bijlipayTransaction.setUrn(String.valueOf(o[count++]));
                bijlipayTransaction.setBin(String.valueOf(o[count]));

                transactions.add(bijlipayTransaction);
                count = 0;
            });
            if (transactions.size() > 0) {
                try {
                    logger.info("Missing SWITCH TXN List Size : {}", transactions.size());
                    bijlipayTransactionRepository.saveAll(transactions);
                    logger.info("Missing SWITCH TXN - Saved in to the DB : ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public double getAmount(Page<SwitchTransactionsDto> response) {
        return response.getContent().stream()
                .filter(l -> "00".equalsIgnoreCase(l.getTxnResponseCode()) &&
                        !("36".equalsIgnoreCase(l.getTxnType()) ||
                                "38".equalsIgnoreCase(l.getTxnType()) ||
                                "48".equalsIgnoreCase(l.getTxnType())))
                .mapToDouble(SwitchTransactionsDto::getTxnAmount)
                .sum();
    }

    @Override
    public double getQrAmount(Page<AxisQrTransactionDto> response) {
        return response.getContent().stream()
                .mapToDouble(s -> Double.parseDouble(s.getTransactionAmount()))
                .sum();
    }

    @Override
    public Double getAxisQrTransactionDetailsAmount(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<List<String>> vpa) throws ParseException {
        if (action.isPresent()) {
            switch (value.get()) {
                case DATE_RANGE_THIS_MONTH: {
                    return staticQrTxnRepository.findAxisQrTxnThisMonthDataSum(vpa.orElse(vpaDetails));
                }
                case DATE_RANGE_LAST_30_DAYS: {
                    return staticQrTxnRepository.findAxisQrTxnLast30DaysDataAmount(vpa.orElse(vpaDetails));
                }
                case DATE_RANGE_CHOOSE_DATE: {
                    if (vpa.isPresent()) {
                        if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", vfrom, vto, vpaDetails.get(0), searchTerm.get());
                            return staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTermAmount(vfrom, vto, vpa.get(), searchTerm.get());
                        } else if (from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", vfrom, vto, vpaDetails.get(0));
                            return staticQrTxnRepository.findAxisQrBasedOnVpaWithDatesAmount(vfrom, vto, vpa.get());
                        } else if (searchTerm.isPresent()) {
                            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                            return staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTermAmount(vpa.get(), searchTerm.get());
                        }
                    } else {
                        if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", vfrom, vto, vpaDetails.get(0), searchTerm.get());
                            return staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTermAmount(vfrom, vto, Collections.singletonList(vpaDetails.get(0)), searchTerm.get());
                        } else if (from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", vfrom, vto, vpaDetails.get(0));
                            return staticQrTxnRepository.findAxisQrBasedOnVpaWithDatesAmount(vfrom, vto, Collections.singletonList(vpaDetails.get(0)));
                        } else if (searchTerm.isPresent()) {
                            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                            return staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTermAmount(Collections.singletonList(vpaDetails.get(0)), searchTerm.get());
                        }
                    }
                }
            }
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return staticQrTxnRepository.findAxisQrTxnAmount(Collections.singletonList(vpaDetails.get(0)));
        } else if (searchTerm.isPresent()) {
            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
            return staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTermAmount(vpa.orElseGet(() -> Collections.singletonList(vpaDetails.get(0))), searchTerm.get());
        } else {
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return staticQrTxnRepository.findAxisQrTxnAmount(vpa.orElseGet(() -> Collections.singletonList(vpaDetails.get(0))));
        }
    }

    @Override
    public Double getSwitchTransactionsSearchAmount(Optional<Integer> action, List<String> tidList, Optional<List<String>> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm) throws ParseException {

        Double amount = null;

            if (action.isPresent()) {
            if (action.get() == TXN_RESPONSE_CODE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get());
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return amount;
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get());
                        }
                    } else {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))));
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return amount;
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))));
                        }
                    }
                }
            } else if (action.get() == TXN_TYPE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (!requestDto.getTxnType().isEmpty()) {
                    if (searchTerm.isPresent()) {
                        return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get());
                    } else {
                        return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType());
                    }
                }
            } else if (action.get() == DATE_RANGE) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getDateRange() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return amount = bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get());
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return amount = bijlipayTransactionRepository.findTransactionDataWithDateRange3Amount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto);
                        }
                    }
                }
            } else if (action.get() == ALL_FILTER) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ALL ACTION ------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                                return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get())
                                        ;
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                                return amount;
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                                return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get());
                            }
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                                return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3Amount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto);
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                                return amount;
                            } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                                return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3Amount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto);
                            }
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty()) {
                    logger.info(" ACTION txn status and txn type------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get());
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return amount;
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), searchTerm.get());
                        }
                    } else {
                        if (requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType());
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return amount;
                        } else if (requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return amount = bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType());
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn status and date range------------------------: {}", action.get());
                    String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                    String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return amount = bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get());
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return amount;
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return amount = bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto, searchTerm.get());
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_SUCCESS) {
                            return amount = bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3Amount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto);
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_FAILURE) {
                            return amount;
                        } else if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && requestDto.getTxnStatus() == TXN_RESPONSE_BOTH_SUCCESS_FAILURE) {
                            return amount = bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3Amount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), vfrom, vto);
                        }

                    }
                } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn type and date range------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return amount = bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto, searchTerm.get());
                        }
                    } else {
                        if (requestDto.getDateRange() == DATE_RANGE_CHOOSE_DATE && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate(from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate(to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return amount = bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3Amount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), requestDto.getTxnType(), vfrom, vto);
                        }
                    }
                }
            }
        } else if (searchTerm.isPresent()) {
                    logger.info("WITHOUT ACTION------------------------:");
                    return amount = bijlipayTransactionRepository.findAllTransactionDataWithSearchAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))), searchTerm.get());
                } else {
                    logger.info("WITHOUT ACTION------------------------:");
                    return amount = bijlipayTransactionRepository.findAllTransactionDataAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))));
                }
                return amount = bijlipayTransactionRepository.findAllTransactionDataAmount(tid.orElseGet(() -> Collections.singletonList(tidList.get(0))));
            }

    @Override
    @Transactional
    public BijlipayTransaction store(JupiterTxnData jupiterTxnData) throws ParseException {
        BijlipayTransaction txnData = new BijlipayTransaction();
            txnData.setMti(jupiterTxnData.getMti() != null ? jupiterTxnData.getMti() : "");
            txnData.setTxnType(jupiterTxnData.getTxnType() != null ? jupiterTxnData.getTxnType() : "");
            txnData.setTerminalId(jupiterTxnData.getTerminalId() != null ? jupiterTxnData.getTerminalId() : "");
            txnData.setMerchantId(jupiterTxnData.getMerchantId() != null ? jupiterTxnData.getMerchantId() : "");
            txnData.setTxnDate(jupiterTxnData.getTxnDate() != null ? jupiterTxnData.getTxnDate() : "");
            txnData.setTxnTime(jupiterTxnData.getTxnTime() != null ? jupiterTxnData.getTxnTime() : "");
            if(jupiterTxnData.getTxnAmount()!=null) {
                String amt = jupiterTxnData.getTxnAmount();
                long parseAmt = Long.parseLong(amt);
                Double txnAmt = parseAmt/100.0;
                String formattedAmt = String.format("%.2f", txnAmt);
                txnData.setTxnAmount(formattedAmt);
                logger.info("amt {} parsed Amt {}",amt, formattedAmt);
            }
            txnData.setTxnResponseCode(jupiterTxnData.getTxnResponseCode() != null ? jupiterTxnData.getTxnResponseCode() : "");
            if (jupiterTxnData.getResponseReceivedTime() != null) {
                String responseReceivedTime = jupiterTxnData.getResponseReceivedTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = dateFormat.parse(responseReceivedTime);
                txnData.setResponseReceivedTime(date);
                txnData.setTxnCorrelationId(date);
            }
            txnData.setrRNumber(jupiterTxnData.getRrn() != null ? jupiterTxnData.getRrn() : "");
            txnData.setStan(jupiterTxnData.getStan() != null ? jupiterTxnData.getStan() : "");
            txnData.setInvoiceNumber(jupiterTxnData.getInvoiceNumber() != null ? jupiterTxnData.getInvoiceNumber() : "");
            txnData.setBatchNumber(jupiterTxnData.getBatchNumber() != null ? jupiterTxnData.getBatchNumber() : "null");
            if(!(jupiterTxnData.getTxnAdditionalAmount().isEmpty())) {
                String amt = jupiterTxnData.getTxnAdditionalAmount();
                long parseAmt = Long.parseLong(amt);
                Double txnAmt = parseAmt/100.0;
                String formattedAmt = String.format("%.2f", txnAmt);
                txnData.setTxnAdditionalAmount(formattedAmt);
                logger.info("amt {} parsed Amt {}",amt, formattedAmt);
            }
  //          txnData.setTxnAdditionalAmount(jupiterTxnData.getTxnAdditionalAmount() != null ? jupiterTxnData.getTxnAdditionalAmount() : "");
            txnData.setMaskedcardnumber(jupiterTxnData.getMaskedCardNumber() != null ? jupiterTxnData.getMaskedCardNumber() : "");
            txnData.setAuthCode(jupiterTxnData.getAuthCode() != null ? jupiterTxnData.getAuthCode() : "null");


         return    txnRepository.save(txnData);
    }
}
