package com.bijlipay.service.implementation;

import com.bijlipay.controller.BaseController;
import com.bijlipay.model.Category;
import com.bijlipay.model.Invoice;
import com.bijlipay.model.Notification;
import com.bijlipay.model.SettlementRequestResponse;
import com.bijlipay.model.User;
import com.bijlipay.model.UserTidVpaData;
import com.bijlipay.model.dto.AxisQrTransactionDto;
import com.bijlipay.model.dto.ForgotPasswordEmail;
import com.bijlipay.model.dto.JupiterTxnData;
import com.bijlipay.model.dto.NotificationBodyDtoForQr;
import com.bijlipay.model.dto.SettlementNotificationDto;
import com.bijlipay.model.dto.SwitchTransactionsDto;
import com.bijlipay.model.dto.TransactionReportDto;
import com.bijlipay.model.dto.TransactionRequestDto;
import com.bijlipay.model.dto.notification.NotificationDto;
import com.bijlipay.model.dto.notification.PushNotificationResponseDto;
import com.bijlipay.model.dto.notification.SettlementNotificationUpdate;
import com.bijlipay.repository.*;
import com.bijlipay.service.BijlipayTransactionService;
import com.bijlipay.service.FileStorageService;
import com.bijlipay.txndb.model.BijlipayTransaction;
import com.bijlipay.txndb.model.StaticQrTxn;
import com.bijlipay.txndb.repository.BijlipayTransactionRepository;
import com.bijlipay.txndb.repository.StaticQrTxnRepository;
import com.bijlipay.util.CardUtil;
import com.bijlipay.util.Constant;
import com.bijlipay.util.DateUtil;
import com.bijlipay.util.MailHandler;
import com.bijlipay.util.ReportUtils;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.mail.MessagingException;
import javax.persistence.Tuple;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<Object[]> getSumOfTodayAndYesterdayVoidAndReversalTxnAmount(String tid, long sessionUserId) {
        new ArrayList();
        List<Object[]> obj = this.bijlipayTransactionRepository.fetchSumOfVoidReversalTxnAmountSwitchTxn(tid);
        return obj;
    }

    public List<SwitchTransactionsDto> getLastFiveSwicthTransactions(String tid) throws IOException {
        List<Tuple> switchTranDto = this.bijlipayTransactionRepository.findLastFiveSwitchTransaction(tid);
        return this.mapSwithTxnDto(switchTranDto);
    }

    public List<SwitchTransactionsDto> getLastFiveSwicthTransactionsBasedOnUser(List<String> tid) {
        List<Tuple> switchTranDto = this.bijlipayTransactionRepository.findLastFiveSwitchTransactionBasedOnUser(tid);
        return this.mapSwithTxnDto(switchTranDto);
    }

    public List<SwitchTransactionsDto> getLastFiveSwicthTransactionsBasedOnUserPortal(List<String> tid) {
        List<Tuple> switchTranDto = this.bijlipayTransactionRepository.findLastFiveSwitchTransactionBasedOnUser(tid);
        return this.mapSwithTxnDtoPortal(switchTranDto);
    }

    public List<SwitchTransactionsDto> mapSwithTxnDto(List<Tuple> tuples) {
        List<SwitchTransactionsDto> switchTransactionsDtos = new ArrayList();
        tuples.stream().forEach((l) -> {
            logger.info("THe tuples : --------- : {}", l.get(9));
            SwitchTransactionsDto switchTransactionsDto1 = new SwitchTransactionsDto();
            switchTransactionsDto1.setId(l.get(0) != null ? (BigInteger)l.get(0) : null);
            switchTransactionsDto1.setMti(l.get(1) != null ? (String)l.get(1) : "");
            switchTransactionsDto1.setTxnType(l.get(2) != null ? (String)l.get(2) : "");
            switchTransactionsDto1.setTerminalId(l.get(3) != null ? (String)l.get(3) : "");
            switchTransactionsDto1.setMerchantId(l.get(4) != null ? (String)l.get(4) : "");
            switchTransactionsDto1.setTxnDate(l.get(5) != null ? (String)l.get(5) : "");
            switchTransactionsDto1.setTxnTime(l.get(6) != null ? (String)l.get(6) : "");
            switchTransactionsDto1.setTxnAmount(l.get(7) != null ? (Double)l.get(7) : (double)0.0F);
            switchTransactionsDto1.setTxnResponseCode(l.get(8) != null ? (String)l.get(8) : null);
            switchTransactionsDto1.setResponseReceivedTime(l.get(9) != null ? DateUtil.convertToString((Date)l.get(9), "dd MMM yyyy hh.mm a") : null);
            switchTransactionsDto1.setrRNumber(l.get(10) != null ? (String)l.get(10) : "");
            switchTransactionsDto1.setStan(l.get(11) != null ? (String)l.get(11) : "");
            switchTransactionsDto1.setInvoiceNumber(l.get(12) != null ? (String)l.get(12) : "");
            switchTransactionsDto1.setBatchNumber(l.get(13) != null ? (String)l.get(13) : "");
            switchTransactionsDto1.setTxnAdditionalAmount(l.get(14) != null ? (Double)l.get(14) : (double)0.0F);
            switchTransactionsDto1.setMaskedCardNumber(l.get(15) != null ? (String)l.get(15) : "");
            switchTransactionsDto1.setPanEntryMode(l.get(16) != null ? (String)l.get(16) : "");
            switchTransactionsDto1.setBin(l.get(17) != null ? (String)l.get(17) : "");
            switchTransactionsDto1.setCustomerVpa(l.get(18) != null ? (String)l.get(18) : "");
            switchTransactionsDtos.add(switchTransactionsDto1);
            logger.info("The transactions : ------------------------------ : {}", switchTransactionsDto1.getId());
        });
        return switchTransactionsDtos;
    }

    public List<SwitchTransactionsDto> mapSwithTxnDtoPortal(List<Tuple> tuples) {
        List<SwitchTransactionsDto> switchTransactionsDtos = new ArrayList();
        tuples.stream().forEach((l) -> {
            logger.info("THe tuples : --------- : {}", l.get(9));
            SwitchTransactionsDto switchTransactionsDto1 = new SwitchTransactionsDto();
            switchTransactionsDto1.setId(l.get(0) != null ? (BigInteger)l.get(0) : null);
            switchTransactionsDto1.setMti(l.get(1) != null ? (String)l.get(1) : "");
            switchTransactionsDto1.setTxnType(l.get(2) != null ? (String)l.get(2) : "");
            switchTransactionsDto1.setTerminalId(l.get(3) != null ? (String)l.get(3) : "");
            switchTransactionsDto1.setMerchantId(l.get(4) != null ? (String)l.get(4) : "");
            switchTransactionsDto1.setTxnDate(l.get(5) != null ? (String)l.get(5) : "");
            switchTransactionsDto1.setTxnTime(l.get(6) != null ? (String)l.get(6) : "");
            switchTransactionsDto1.setTxnAmount(l.get(7) != null ? (Double)l.get(7) : (double)0.0F);
            switchTransactionsDto1.setTxnResponseCode(l.get(8) != null ? (String)l.get(8) : null);
            switchTransactionsDto1.setResponseReceivedTime(l.get(9) != null ? DateUtil.convertToString((Date)l.get(9), "dd MMM yyyy hh.mm a") : null);
            switchTransactionsDto1.setrRNumber(l.get(10) != null ? (String)l.get(10) : "");
            switchTransactionsDto1.setStan(l.get(11) != null ? (String)l.get(11) : "");
            switchTransactionsDto1.setInvoiceNumber(l.get(12) != null ? (String)l.get(12) : "");
            switchTransactionsDto1.setBatchNumber(l.get(13) != null ? (String)l.get(13) : "");
            switchTransactionsDto1.setTxnAdditionalAmount(l.get(14) != null ? (Double)l.get(14) : (double)0.0F);
            switchTransactionsDto1.setMaskedCardNumber(l.get(15) != null ? (String)l.get(15) : "");
            switchTransactionsDto1.setPanEntryMode(l.get(16) != null ? (String)l.get(16) : "");
            switchTransactionsDto1.setBin(l.get(17) != null ? (String)l.get(17) : "");
            switchTransactionsDto1.setCustomerVpa(l.get(18) != null ? (String)l.get(18) : "");
            String var10001 = (String)l.get(1);
            switchTransactionsDto1.setTxnStatus(Constant.getTxnTypeStatus(var10001 + String.valueOf(l.get(2)) + String.valueOf(l.get(8))));
            switchTransactionsDto1.setTxnMode(l.get(17) != null && !l.get(17).toString().equalsIgnoreCase("null") && !l.get(17).toString().equalsIgnoreCase("") ? CardUtil.getCardType(Integer.parseInt((String)l.get(17))) : "");
            switchTransactionsDtos.add(switchTransactionsDto1);
            logger.info("The transactions : ------------------------------ : {}", switchTransactionsDto1.getId());
        });
        return switchTransactionsDtos;
    }

    public Page<SwitchTransactionsDto> mapSwithTxnDtoPagination(Page<Object[]> txnDatas) {
        List<Object[]> tuples = txnDatas.getContent();
        List<SwitchTransactionsDto> switchTransactionsDtos = new ArrayList();
        tuples.stream().forEach((l) -> {
            logger.info("THe tuples : --------- : {}", l[9]);
            SwitchTransactionsDto switchTransactionsDto1 = new SwitchTransactionsDto();
            switchTransactionsDto1.setId(l[0] != null ? (BigInteger)l[0] : null);
            switchTransactionsDto1.setMti(l[1] != null ? (String)l[1] : "");
            switchTransactionsDto1.setTxnType(l[2] != null ? (String)l[2] : "");
            switchTransactionsDto1.setTerminalId(l[3] != null ? (String)l[3] : "");
            switchTransactionsDto1.setMerchantId(l[4] != null ? (String)l[4] : "");
            switchTransactionsDto1.setTxnDate(l[5] != null ? (String)l[5] : "");
            switchTransactionsDto1.setTxnTime(l[6] != null ? (String)l[6] : "");
            switchTransactionsDto1.setTxnAmount(l[7] != null ? (Double)l[7] : (double)0.0F);
            switchTransactionsDto1.setTxnResponseCode(l[8] != null ? (String)l[8] : null);
            switchTransactionsDto1.setResponseReceivedTime(l[9] != null ? DateUtil.convertToString((Date)l[9], "dd MMM yyyy hh.mm a") : null);
            switchTransactionsDto1.setrRNumber(l[10] != null ? (String)l[10] : "");
            switchTransactionsDto1.setStan(l[11] != null ? (String)l[11] : "");
            switchTransactionsDto1.setInvoiceNumber(l[12] != null ? (String)l[12] : "");
            switchTransactionsDto1.setBatchNumber(l[13] != null ? (String)l[13] : "");
            switchTransactionsDto1.setTxnAdditionalAmount(l[14] != null ? (Double)l[14] : (double)0.0F);
            switchTransactionsDto1.setMaskedCardNumber(l[15] != null ? (String)l[15] : "");
            switchTransactionsDto1.setPanEntryMode(l[16] != null ? (String)l[16] : "");
            switchTransactionsDto1.setBin(l[17] != null ? (String)l[17] : "");
            switchTransactionsDto1.setCustomerVpa(l[18] != null ? (String)l[18] : "");
            switchTransactionsDtos.add(switchTransactionsDto1);
            logger.info("The transactions : ------------------------------ : {}", switchTransactionsDto1.getId());
        });
        return new PageImpl(switchTransactionsDtos, PageRequest.of(txnDatas.getNumber(), txnDatas.getSize(), txnDatas.getSort()), txnDatas.getTotalElements());
    }

    public Page<SwitchTransactionsDto> mapSwithTxnDtoPaginationForPortal(Page<Object[]> txnDatas) {
        List<Object[]> tuples = txnDatas.getContent();
        List<SwitchTransactionsDto> switchTransactionsDtos = new ArrayList();
        tuples.stream().forEach((l) -> {
            logger.info("THe tuples : --------- : {}", l[9]);
            SwitchTransactionsDto switchTransactionsDto1 = new SwitchTransactionsDto();
            switchTransactionsDto1.setId(l[0] != null ? (BigInteger)l[0] : null);
            switchTransactionsDto1.setMti(l[1] != null ? (String)l[1] : "");
            switchTransactionsDto1.setTxnType(l[2] != null ? (String)l[2] : "");
            switchTransactionsDto1.setTerminalId(l[3] != null ? (String)l[3] : "");
            switchTransactionsDto1.setMerchantId(l[4] != null ? (String)l[4] : "");
            switchTransactionsDto1.setTxnDate(l[5] != null ? (String)l[5] : "");
            switchTransactionsDto1.setTxnTime(l[6] != null ? (String)l[6] : "");
            switchTransactionsDto1.setTxnAmount(l[7] != null ? (Double)l[7] : (double)0.0F);
            switchTransactionsDto1.setTxnResponseCode(l[8] != null ? (String)l[8] : null);
            switchTransactionsDto1.setResponseReceivedTime(l[9] != null ? DateUtil.convertToString((Date)l[9], "dd MMM yyyy hh.mm a") : null);
            switchTransactionsDto1.setrRNumber(l[10] != null ? (String)l[10] : "");
            switchTransactionsDto1.setStan(l[11] != null ? (String)l[11] : "");
            switchTransactionsDto1.setInvoiceNumber(l[12] != null ? (String)l[12] : "");
            switchTransactionsDto1.setBatchNumber(l[13] != null ? (String)l[13] : "");
            switchTransactionsDto1.setTxnAdditionalAmount(l[14] != null ? (Double)l[14] : (double)0.0F);
            switchTransactionsDto1.setMaskedCardNumber(l[15] != null ? (String)l[15] : "");
            switchTransactionsDto1.setPanEntryMode(l[16] != null ? (String)l[16] : "");
            switchTransactionsDto1.setBin(l[17] != null ? (String)l[17] : "");
            switchTransactionsDto1.setCustomerVpa(l[18] != null ? (String)l[18] : "");
            logger.info("status - -----: l1{},l2{},l8{} -------txn Status{}", new Object[]{l[1], l[2], l[8], Constant.getTxnTypeStatus((String)l[1] + String.valueOf(l[2]) + String.valueOf(l[8]))});
            switchTransactionsDto1.setTxnStatus(Constant.getTxnTypeStatus((String)l[1] + String.valueOf(l[2]) + String.valueOf(l[8])));
            switchTransactionsDto1.setTxnMode(l[17] != null && l[17].toString().equalsIgnoreCase("null") && l[17].toString().equalsIgnoreCase("") ? CardUtil.getCardType(Integer.parseInt((String)l[17])) : "");
            switchTransactionsDtos.add(switchTransactionsDto1);
            logger.info("The transactions : ------------------------------ : {}", switchTransactionsDto1.getId());
        });
        return new PageImpl(switchTransactionsDtos, PageRequest.of(txnDatas.getNumber(), txnDatas.getSize(), txnDatas.getSort()), txnDatas.getTotalElements());
    }

    public List<AxisQrTransactionDto> getLastFiveAxisQrTransactions(String vpaDetails) throws SQLException {
        List<Tuple> staticQrTxnData = this.staticQrTxnRepository.findLastFiveAxisStaticQrTxn(vpaDetails);
        return this.mapAxisQrTxnDto(staticQrTxnData);
    }

    public List<AxisQrTransactionDto> getLastFiveAxisQrTransactionsBasedOnUser(List<String> vpaDetails) {
        List<Tuple> staticQrTxnData = this.staticQrTxnRepository.findLastFiveAxisStaticQrTxnBasedOnUser(vpaDetails);
        return this.mapAxisQrTxnDto(staticQrTxnData);
    }

    public List<AxisQrTransactionDto> getLastFiveAxisQrTransactionsBasedOnUserPortal(List<String> vpaDetails) {
        List<Tuple> staticQrTxnData = this.staticQrTxnRepository.findLastFiveAxisStaticQrTxnBasedOnUser(vpaDetails);
        return this.mapAxisQrTxnDtoPortal(staticQrTxnData);
    }

    public List<AxisQrTransactionDto> mapAxisQrTxnDto(List<Tuple> transactionDatas) {
        List<AxisQrTransactionDto> axisQrTransactionDtos = new ArrayList();
        transactionDatas.forEach((l) -> {
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
        List<AxisQrTransactionDto> axisQrTransactionDtos = new ArrayList();
        transactionDatas.forEach((l) -> {
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
        List<AxisQrTransactionDto> axisQrTransactionDtos = new ArrayList();
        content.forEach((l) -> {
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
        return new PageImpl(axisQrTransactionDtos, PageRequest.of(transactionDatas.getNumber(), transactionDatas.getSize(), transactionDatas.getSort()), transactionDatas.getTotalElements());
    }

    public List<Object[]> getSumAndCountOfTodayAndYesterdayTxnAmountForTid(String tid, long sessionUserId) {
        List<Object[]> result = new ArrayList();
        Object[] data = new Object[4];
        List<Object[]> consolidated = this.bijlipayTransactionRepository.fetchConsolidatedTodayYesterdaySums(tid);
        double todaySumPos200 = (double)0.0F;
        double todaySumPos400 = (double)0.0F;
        double todaySumVoid = (double)0.0F;
        double todaySumUpi37 = (double)0.0F;
        double todaySumUpi39 = (double)0.0F;
        long todayCountPos200 = 0L;
        long todayCountPos400 = 0L;
        long todayCountVoid = 0L;
        long todayCountUpi37 = 0L;
        long todayCountUpi39 = 0L;
        double yesterdaySumPos200 = (double)0.0F;
        double yesterdaySumPos400 = (double)0.0F;
        double yesterdaySumVoid = (double)0.0F;
        double yesterdaySumUpi37 = (double)0.0F;
        double yesterdaySumUpi39 = (double)0.0F;
        long yesterdayCountPos200 = 0L;
        long yesterdayCountPos400 = 0L;
        long yesterdayCountVoid = 0L;
        long yesterdayCountUpi37 = 0L;
        long yesterdayCountUpi39 = 0L;

        for(Object[] row : consolidated) {
            String mti = row[0] != null ? row[0].toString() : "";
            String txnType = row[1] != null ? row[1].toString() : "";
            double tSum = row[2] != null ? ((Number)row[2]).doubleValue() : (double)0.0F;
            long tCount = row[3] != null ? ((Number)row[3]).longValue() : 0L;
            double ySum = row[4] != null ? ((Number)row[4]).doubleValue() : (double)0.0F;
            long yCount = row[5] != null ? ((Number)row[5]).longValue() : 0L;
            if ("0200".equals(mti) && "00".equals(txnType)) {
                todaySumPos200 = tSum;
                todayCountPos200 = tCount;
                yesterdaySumPos200 = ySum;
                yesterdayCountPos200 = yCount;
            } else if ("0200".equals(mti) && "02".equals(txnType)) {
                todaySumVoid = tSum;
                todayCountVoid = tCount;
                yesterdaySumVoid = ySum;
                yesterdayCountVoid = yCount;
            } else if ("0400".equals(mti) && "00".equals(txnType)) {
                todaySumPos400 = tSum;
                todayCountPos400 = tCount;
                yesterdaySumPos400 = ySum;
                yesterdayCountPos400 = yCount;
            } else if ("0100".equals(mti) && "37".equals(txnType)) {
                todaySumUpi37 = tSum;
                todayCountUpi37 = tCount;
                yesterdaySumUpi37 = ySum;
                yesterdayCountUpi37 = yCount;
            } else if ("0100".equals(mti) && "39".equals(txnType)) {
                todaySumUpi39 = tSum;
                todayCountUpi39 = tCount;
                yesterdaySumUpi39 = ySum;
                yesterdayCountUpi39 = yCount;
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

    public List<Object[]> getSumAndCountOfTodayAndYesterdayTxnAmountForVpa(String vpa, long sessionUserId) {
        return this.staticQrTxnRepository.fetchSumAndCountOfTodayAndYesterdayTxn(vpa);
    }

    public Page<AxisQrTransactionDto> getAxisQrTransactionDetails(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<List<String>> vpa, Pageable pageable) throws ParseException {
        if (action.isPresent()) {
            switch ((Integer)value.get()) {
                case 1:
                    return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrTxnThisMonthData((String)vpaDetails.get(0), pageable));
                case 2:
                    return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrTxnLast30DaysData(vpaDetails, pageable));
                case 3:
                    if (vpa.isPresent()) {
                        if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", new Object[]{vfrom, vto, vpaDetails.get(0), searchTerm.get()});
                            return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTerm(vfrom, vto, (List)vpa.get(), (String)searchTerm.get(), pageable));
                        } else if (from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", new Object[]{vfrom, vto, vpaDetails.get(0)});
                            return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrBasedOnVpaWithDates(vfrom, vto, (List)vpa.get(), pageable));
                        } else if (searchTerm.isPresent()) {
                            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                            return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm((List)vpa.get(), (String)searchTerm.get(), pageable));
                        }
                    } else if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", new Object[]{vfrom, vto, vpaDetails.get(0), searchTerm.get()});
                        return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTerm(vfrom, vto, Collections.singletonList((String)vpaDetails.get(0)), (String)searchTerm.get(), pageable));
                    } else if (from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", new Object[]{vfrom, vto, vpaDetails.get(0)});
                        return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrBasedOnVpaWithDates(vfrom, vto, Collections.singletonList((String)vpaDetails.get(0)), pageable));
                    } else if (searchTerm.isPresent()) {
                        logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                        return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm(Collections.singletonList((String)vpaDetails.get(0)), (String)searchTerm.get(), pageable));
                    }
                default:
                    logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
                    return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrTxn(Collections.singletonList((String)vpaDetails.get(0)), pageable));
            }
        } else if (searchTerm.isPresent()) {
            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
            return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm((List)vpa.orElseGet(() -> Collections.singletonList((String)vpaDetails.get(0))), (String)searchTerm.get(), pageable));
        } else {
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return this.mapAxisQrTxnDtoPagination(this.staticQrTxnRepository.findAxisQrTxn((List)vpa.orElseGet(() -> Collections.singletonList((String)vpaDetails.get(0))), pageable));
        }
    }

    public Page<SwitchTransactionsDto> getSwitchTransactionsSearch(Optional<Integer> action, List<String> tidList, Optional<List<String>> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm, Pageable pageable) throws ParseException {
        logger.info("SWITCH_TXN : entered into the section :");
        if (!action.isPresent()) {
            if (searchTerm.isPresent()) {
                logger.info("WITHOUT ACTION------------------------:");
                return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findAllTransactionDataWithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get(), pageable));
            } else {
                logger.info("WITHOUT ACTION------------------------:");
                return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findAllTransactionData((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), pageable));
            }
        } else {
            if ((Integer)action.get() == 1) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailureSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccess((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), pageable));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailure((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), pageable));
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailure((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), pageable));
                        }
                    }
                }
            } else if ((Integer)action.get() == 2) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (!requestDto.getTxnType().isEmpty()) {
                    if (searchTerm.isPresent()) {
                        return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                    }

                    return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnType((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                }
            } else if ((Integer)action.get() == 3) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getDateRange() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get(), pageable));
                        }
                    } else if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataWithDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, pageable));
                    }
                }
            } else if ((Integer)action.get() == 4) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ALL ACTION ------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == 1) {
                                return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                            }

                            if (requestDto.getTxnStatus() == 2) {
                                if (requestDto.getTxnType().size() > 2) {
                                    logger.info("requestDto txnType all 3 filter search------- : {}", requestDto.getTxnType());
                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                        requestDto.getTxnType().removeAll(List.of("38", "48"));
                                        logger.info("after removing 1-----------------:{}", requestDto.getTxnType());
                                        return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                                    }
                                } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                                }

                                return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                            }

                            if (requestDto.getTxnStatus() == 3) {
                                return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                            }
                        }
                    } else if (requestDto.getDateRange() == 3) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        if (requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            if (requestDto.getTxnType().size() > 2) {
                                logger.info("requestDto txnType all 3 filter without tid and search------- : {}", requestDto.getTxnType());
                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("after removing 2-----------------:{}", requestDto.getTxnType());
                                    return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                                }
                            } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                            }

                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty()) {
                    logger.info(" ACTION txn status and txn type------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            logger.info("txn type --------------------- : {}, ------ {}", requestDto.getTxnType().size(), requestDto.getTxnType());
                            if (requestDto.getTxnType().size() > 2) {
                                logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("after removing 3----------------:{}", requestDto.getTxnType());
                                    return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                                }
                            } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                            }

                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndType((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            logger.info("requestDto txnType any 2 filter without tid and search------- : {}", requestDto.getTxnType());
                            if (requestDto.getTxnType().size() > 2) {
                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("after removing 4-----------------:{}", requestDto.getTxnType());
                                    return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                                }
                            } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                            }

                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn status and date range------------------------: {}", action.get());
                    String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                    String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 2) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, pageable));
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 2) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, pageable));
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, pageable));
                        }
                    }
                } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn type and date range------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                        }
                    } else if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                    }
                }
            }

            return this.mapSwithTxnDtoPagination(this.bijlipayTransactionRepository.findAllTransactionData((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), pageable));
        }
    }

    public Page<SwitchTransactionsDto> getSwitchTransactionsSearchData(Optional<Integer> action, List<String> tidList, Optional<List<String>> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm, Pageable pageable) throws ParseException {
        logger.info("SWITCH_TXN : entered into the section :");
        if (!action.isPresent()) {
            if (searchTerm.isPresent()) {
                logger.info("WITHOUT ACTION------------------------:");
                return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findAllTransactionDataWithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get(), pageable));
            } else {
                logger.info("WITHOUT ACTION------------------------:");
                return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findAllTransactionData((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), pageable));
            }
        } else {
            if ((Integer)action.get() == 1) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailureSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccess((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), pageable));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailure((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), pageable));
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailure((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), pageable));
                        }
                    }
                }
            } else if ((Integer)action.get() == 2) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (!requestDto.getTxnType().isEmpty()) {
                    if (searchTerm.isPresent()) {
                        return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                    }

                    return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnType((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                }
            } else if ((Integer)action.get() == 3) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getDateRange() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get(), pageable));
                        }
                    } else if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataWithDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, pageable));
                    }
                }
            } else if ((Integer)action.get() == 4) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ALL ACTION ------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == 1) {
                                return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                            }

                            if (requestDto.getTxnStatus() == 2) {
                                if (requestDto.getTxnType().size() > 2) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("requestDto txnType all 3 filter search------- : {}", requestDto.getTxnType());
                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                        return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                                    }
                                } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                                }

                                return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                            }

                            if (requestDto.getTxnStatus() == 3) {
                                return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                            }
                        }
                    } else if (requestDto.getDateRange() == 3) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        if (requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            if (requestDto.getTxnType().size() > 2) {
                                requestDto.getTxnType().removeAll(List.of("38", "48"));
                                logger.info("requestDto txnType all 3 filter without tid and search------- : {}", requestDto.getTxnType());
                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                                }
                            } else if (requestDto.getTxnType().size() == 2 && (new HashSet(requestDto.getTxnType())).contains("38,48")) {
                                return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                            }

                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty()) {
                    logger.info(" ACTION txn status and txn type------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
                            if (requestDto.getTxnType().size() > 2) {
                                requestDto.getTxnType().removeAll(List.of("38", "48"));
                                return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                            }

                            if (requestDto.getTxnType().size() == 2 && (new HashSet(requestDto.getTxnType())).contains("38,48")) {
                                return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                            }

                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndType((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
                            if (requestDto.getTxnType().size() > 2) {
                                requestDto.getTxnType().removeAll(List.of("38", "48"));
                                logger.info("requestDto txnType any 2 filter without tid and search------- : {}", requestDto.getTxnType());
                                return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                            }

                            if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                            }

                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), pageable));
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn status and date range------------------------: {}", action.get());
                    String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                    String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 2) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get(), pageable));
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get(), pageable));
                        }
                    } else {
                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 1) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, pageable));
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 2) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, pageable));
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 3) {
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, pageable));
                        }
                    }
                } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn type and date range------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get(), pageable));
                        }
                    } else if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, pageable));
                    }
                }
            }

            return this.mapSwithTxnDtoPaginationForPortal(this.bijlipayTransactionRepository.findAllTransactionData((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), pageable));
        }
    }

    public InputStreamResource downloadTransactionReport(Optional<Integer> action, List<String> tidList, Optional<String> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm) throws IOException, ParseException {
        new ArrayList();
        if (!action.isPresent()) {
            if (searchTerm.isPresent()) {
                logger.info("WITHOUT ACTION------------------------:");
                return this.updateWorkBook(this.bijlipayTransactionRepository.findAllTransactionDataWithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), (String)searchTerm.get()), tid, tidList, from, to);
            } else {
                logger.info("WITHOUT ACTION------------------------:");
                return this.updateWorkBook(this.bijlipayTransactionRepository.findAllTransactionData((String)tid.orElseGet(() -> (String)tidList.get(0))), tid, tidList, from, to);
            }
        } else {
            if ((Integer)action.get() == 1) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), (String)searchTerm.get()), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailureSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), (String)searchTerm.get()), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), (String)searchTerm.get()), tid, tidList, from, to);
                        }
                    } else {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccess((String)tid.orElseGet(() -> (String)tidList.get(0))), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusFailure((String)tid.orElseGet(() -> (String)tidList.get(0))), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailure((String)tid.orElseGet(() -> (String)tidList.get(0))), tid, tidList, from, to);
                        }
                    }
                }
            } else if ((Integer)action.get() == 2) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (!requestDto.getTxnType().isEmpty()) {
                    if (searchTerm.isPresent()) {
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), (String)searchTerm.get()), tid, tidList, from, to);
                    }

                    return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnType((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                }
            } else if ((Integer)action.get() == 3) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getDateRange() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                        }
                    } else if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                    }
                }
            } else if ((Integer)action.get() == 4) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ALL ACTION ------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == 1) {
                                return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                            }

                            if (requestDto.getTxnStatus() == 2) {
                                if (requestDto.getTxnType().size() > 2) {
                                    requestDto.getTxnType().removeAll(List.of("38", "48"));
                                    logger.info("requestDto txnType all 3 filter search------- : {}", requestDto.getTxnType());
                                    if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                                    }
                                } else if (requestDto.getTxnType().size() == 2 && requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                                }

                                return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                            }

                            if (requestDto.getTxnStatus() == 3) {
                                return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                            }
                        }
                    } else if (requestDto.getDateRange() == 3) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        if (requestDto.getTxnStatus() == 1) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            if (requestDto.getTxnType().size() > 2) {
                                requestDto.getTxnType().removeAll(List.of("38", "48"));
                                logger.info("requestDto txnType all 3 filter without tid and search------- : {}", requestDto.getTxnType());
                                if (requestDto.getTxnType().contains("38") && requestDto.getTxnType().contains("48")) {
                                    return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3848((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                                }
                            } else if (requestDto.getTxnType().size() == 2 && (new HashSet(requestDto.getTxnType())).contains("38,48")) {
                                return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                            }

                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty()) {
                    logger.info(" ACTION txn status and txn type------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), (String)searchTerm.get()), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            if (requestDto.getTxnType().size() > 2) {
                                requestDto.getTxnType().removeAll(List.of("38", "48"));
                                logger.info("requestDto txnType any 2 filter search------- : {}", requestDto.getTxnType());
                                return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), (String)searchTerm.get()), tid, tidList, from, to);
                            }

                            if (requestDto.getTxnType().size() == 2 && (new HashSet(requestDto.getTxnType())).contains("38,48")) {
                                return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), (String)searchTerm.get()), tid, tidList, from, to);
                            }

                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), (String)searchTerm.get()), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), (String)searchTerm.get()), tid, tidList, from, to);
                        }
                    } else {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndType((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            if (requestDto.getTxnType().size() > 2) {
                                requestDto.getTxnType().removeAll(List.of("38", "48"));
                                logger.info("requestDto txnType any 2 filter without tid and search------- : {}", requestDto.getTxnType());
                                return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType3848((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                            }

                            if (requestDto.getTxnType().size() == 2 && (new HashSet(requestDto.getTxnType())).contains("38,48")) {
                                return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                            }

                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn status and date range------------------------: {}", action.get());
                    String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                    String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                    if (!searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 1) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 2) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 3) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndType((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                        }

                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnFailureAndType((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType()), tid, tidList, from, to);
                    }

                    if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 1) {
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                    }

                    if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 2) {
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                    }

                    if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 3) {
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                    }
                }
            } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
                logger.info(" ACTION txn status and date range------------------------: {}", action.get());
                String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                if (searchTerm.isPresent()) {
                    if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 1) {
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                    }

                    if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 2) {
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                    }

                    if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 3) {
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                    }
                } else {
                    if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 1) {
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                    }

                    if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 2) {
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnFailureAndDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                    }

                    if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 3) {
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), vfrom, vto), tid, tidList, from, to);
                    }
                }
            } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                logger.info(" ACTION txn type and date range------------------------: {}", action.get());
                if (searchTerm.isPresent()) {
                    if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                    }
                } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn type and date range------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearch((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get()), tid, tidList, from, to);
                        }
                    } else if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        return this.updateWorkBook(this.bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3((String)tid.orElseGet(() -> (String)tidList.get(0)), requestDto.getTxnType(), vfrom, vto), tid, tidList, from, to);
                    }
                }
            }

            return this.updateWorkBook(this.bijlipayTransactionRepository.findAllTransactionData((String)tid.orElseGet(() -> (String)tidList.get(0))), tid, tidList, from, to);
        }
    }

    private InputStreamResource updateWorkBook(List<Object[]> transactionsDtos, Optional<String> tid, List<String> tidList, Optional<Long> from, Optional<Long> to) throws IOException, ParseException {
        List<TransactionReportDto> result = new ArrayList();
        transactionsDtos.stream().forEach((m) -> {
            TransactionReportDto txnDto = new TransactionReportDto();
            txnDto.setTxnDate(m[0] == null ? "NA" : m[0].toString());
            txnDto.setMid(m[1] == null ? "NA" : m[1].toString());
            txnDto.setPayeeVpaOrTid(m[2] == null ? "NA" : m[2].toString());
            txnDto.setResponseCodeOrTxnType(m[3] == null ? "NA" : m[3].toString());
            txnDto.setBankRrn(m[4] == null ? "NA" : m[4].toString());
            String var10000 = m[7].toString();
            if (Objects.equals(Constant.getTxnType(var10000 + m[3].toString()), "Void")) {
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
            fileName = String.format((String)tid.orElseGet(() -> (String)tidList.get(0)) + "_%s_%s_%s.xlsx", DateUtil.longtoDate((Long)from.get(), "yyyMMdd"), DateUtil.longtoDate((Long)to.get(), "yyyMMdd"), System.currentTimeMillis());
        } else {
            fileName = String.format((String)tid.orElseGet(() -> (String)tidList.get(0)) + "_%s.xlsx", System.currentTimeMillis());
        }

        String filePath = this.fileUploadDir + "/" + fileName;
        InputStreamResource file = ReportUtils.posTxnExcelReport(result, headers, sheetName, filePath);
        UserTidVpaData userTidVpaData = this.userTidVpaDataRepository.findByTid((String)tid.orElseGet(() -> (String)tidList.get(0)));
        SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
        settlementRequestResponse.setUser(userTidVpaData.getUser());
        if (from.isPresent() && to.isPresent()) {
            settlementRequestResponse.setFromDate(DateUtil.longtoDate((Long)from.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setToDate(DateUtil.longtoDate((Long)to.get(), "dd-MM-yyyy"));
        }

        settlementRequestResponse.setTid((String)tid.orElseGet(() -> (String)tidList.get(0)));
        settlementRequestResponse.setMid(userTidVpaData.getMid());
        settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
        logger.info("after 3 month --------- : {}", DateUtil.addThreeMonthDate());
        settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
        settlementRequestResponse.setFileExist(true);
        settlementRequestResponse.setFileUrl(this.fileDownloadUrl + fileName);
        settlementRequestResponse.setFileName(fileName);
        settlementRequestResponse.setCategoryType("Transaction");
        SettlementRequestResponse fileResponse = (SettlementRequestResponse)this.settlementRequestResponseRepository.save(settlementRequestResponse);
        logger.info("settlement request response final ----------: {}", fileResponse);
        return file;
    }

    public InputStreamResource downloadQrTransactionReport(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<String> vpa) throws ParseException {
        if (action.isPresent()) {
            switch ((Integer)value.get()) {
                case 1:
                    return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxnThisMonthData(vpaDetails), vpa, vpaDetails, from, to);
                case 2:
                    return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxnLast30DaysData(vpaDetails), vpa, vpaDetails, from, to);
                case 3:
                    if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", new Object[]{vfrom, vto, vpaDetails.get(0), searchTerm.get()});
                        return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTerm(vfrom, vto, (String)vpa.orElseGet(() -> (String)vpaDetails.get(0)), (String)searchTerm.get()), vpa, vpaDetails, from, to);
                    } else if (from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", new Object[]{vfrom, vto, vpaDetails.get(0)});
                        return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrBasedOnVpaWithDates(vfrom, vto, (String)vpa.orElseGet(() -> (String)vpaDetails.get(0))), vpa, vpaDetails, from, to);
                    } else if (searchTerm.isPresent()) {
                        logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                        return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm((String)vpa.orElseGet(() -> (String)vpaDetails.get(0)), (String)searchTerm.get()), vpa, vpaDetails, from, to);
                    }
                default:
                    logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
                    return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxn((String)vpa.orElseGet(() -> (String)vpaDetails.get(0))), vpa, vpaDetails, from, to);
            }
        } else if (searchTerm.isPresent()) {
            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
            return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm((String)vpa.orElseGet(() -> (String)vpaDetails.get(0)), (String)searchTerm.get()), vpa, vpaDetails, from, to);
        } else {
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxn(vpaDetails), vpa, vpaDetails, from, to);
        }
    }

    public InputStreamResource downloadQrTransactionReportData(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<String> vpa) throws ParseException {
        if (action.isPresent()) {
            switch ((Integer)value.get()) {
                case 1:
                    return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxnThisMonthData(vpaDetails), vpa, vpaDetails, from, to);
                case 2:
                    return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxnLast30DaysData(vpaDetails), vpa, vpaDetails, from, to);
                case 3:
                    if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", new Object[]{vfrom, vto, vpaDetails.get(0), searchTerm.get()});
                        return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTerm(vfrom, vto, (String)vpa.orElseGet(() -> (String)vpaDetails.get(0)), (String)searchTerm.get()), vpa, vpaDetails, from, to);
                    } else if (from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", new Object[]{vfrom, vto, vpaDetails.get(0)});
                        return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrBasedOnVpaWithDates(vfrom, vto, (String)vpa.orElseGet(() -> (String)vpaDetails.get(0))), vpa, vpaDetails, from, to);
                    } else if (searchTerm.isPresent()) {
                        logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                        return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm((String)vpa.orElseGet(() -> (String)vpaDetails.get(0)), (String)searchTerm.get()), vpa, vpaDetails, from, to);
                    }
                default:
                    logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
                    return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxn((String)vpa.orElseGet(() -> (String)vpaDetails.get(0))), vpa, vpaDetails, from, to);
            }
        } else if (searchTerm.isPresent()) {
            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
            return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTerm((String)vpa.orElseGet(() -> (String)vpaDetails.get(0)), (String)searchTerm.get()), vpa, vpaDetails, from, to);
        } else {
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return this.updateQRWorkbook(this.staticQrTxnRepository.findAxisQrTxn((String)vpa.orElseGet(() -> (String)vpaDetails.get(0))), vpa, vpaDetails, from, to);
        }
    }

    private InputStreamResource updateQRWorkbook(List<Object[]> txnList, Optional<String> vpa, List<String> vpaList, Optional<Long> from, Optional<Long> to) throws ParseException {
        List<AxisQrTransactionDto> result = new ArrayList();
        txnList.stream().forEach((m) -> {
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
        if (from.isPresent() && to.isPresent()) {
            fileName = String.format("QrTransactionReport_%s_%s_%s.xlsx", DateUtil.longtoDate((Long)from.get(), "yyyMMdd"), DateUtil.longtoDate((Long)to.get(), "yyyMMdd"), System.currentTimeMillis());
        } else {
            fileName = String.format("QrTransactionReport_%s.xlsx", System.currentTimeMillis());
        }

        String filePath = this.fileUploadDir + "/" + fileName;
        InputStreamResource file = ReportUtils.QrTxnReport(result, headers, sheetName, filePath);
        SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
        settlementRequestResponse.setUser(this.userRepository.findByUser(this.getSessionUser().getId()));
        if (from.isPresent() && to.isPresent()) {
            settlementRequestResponse.setFromDate(DateUtil.longtoDate((Long)from.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setToDate(DateUtil.longtoDate((Long)to.get(), "dd-MM-yyyy"));
        }

        settlementRequestResponse.setVpa((String)vpa.orElseGet(() -> (String)vpaList.get(0)));
        settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
        logger.info("after 3 month --------- : {}", DateUtil.addThreeMonthDate());
        settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
        settlementRequestResponse.setFileExist(true);
        settlementRequestResponse.setFileUrl(this.fileDownloadUrl + fileName);
        settlementRequestResponse.setFileName(fileName);
        settlementRequestResponse.setCategoryType("Transaction");
        SettlementRequestResponse fileResponse = (SettlementRequestResponse)this.settlementRequestResponseRepository.save(settlementRequestResponse);
        logger.info("settlement request response final ----------: {}", fileResponse);
        return file;
    }

    public void requestMailForInvoiceCreation(Invoice resqDto) throws MessagingException {
        Invoice invoice1 = new Invoice();
        invoice1.setInvoiceDate(resqDto.getInvoiceDate());
        invoice1.setInvoiceType(resqDto.getType() == 1 ? "MDR" : "RENTAL");
        invoice1.setEmail(resqDto.getEmail());
        invoice1.setTid(resqDto.getTid());
        Invoice save = (Invoice)this.invoiceRepository.save(invoice1);
        ForgotPasswordEmail invoiceMailRequest = new ForgotPasswordEmail();
        String mailTemplate = "Hi Team, \n\nGenerate Invoice For the Mid - %s and Tid - %s Month - %s,  \n\nsend it to the following mail id - %s \n\nRegards,\nTeam Bijlipay \n\nNote : This is an auto generated email. Please do not respond to this email id.";
        invoiceMailRequest.setSubject(String.format("%s invoice Request", save.getInvoiceType()));
        invoiceMailRequest.setBody(String.format(mailTemplate, this.userTidVpaDataRepository.findByTid(save.getTid()).getMid(), save.getTid(), save.getInvoiceDate(), save.getEmail()));
        invoiceMailRequest.setReceiverEmail(this.invoiceRecMail);
        this.mailHandler.commonMailRequestWithoutAttachment(invoiceMailRequest);
    }

    @Async("asyncExecutor")
    public void pushTxnNotifications(SwitchTransactionsDto switchTransactionsDto) {
        Gson gson = new Gson();
        gson.toJson(switchTransactionsDto);

        try {
            UserTidVpaData userTidVpaData = this.userTidVpaDataRepository.findByTid(switchTransactionsDto.getTerminalId());
            if (userTidVpaData != null && (switchTransactionsDto.getMti().equals("0200") || (switchTransactionsDto.getTxnType().equals("37") || switchTransactionsDto.getTxnType().equals("39")) && switchTransactionsDto.getMti().equalsIgnoreCase("0100")) && switchTransactionsDto.getTxnResponseCode().equals("00")) {
                User cUser = (User)this.userRepository.getOne(userTidVpaData.getUser().getId());
                String var10000 = switchTransactionsDto.getMti();
                String txnType = Constant.getTxnType(var10000 + switchTransactionsDto.getTxnType());
                if (txnType == "050092") {
                    return;
                }

                if (switchTransactionsDto.getMti().equals("0320")) {
                    txnType = "0320";
                } else if (switchTransactionsDto.getMti().equals("0400")) {
                    txnType = "Reversal";
                }

                Category category = new Category();
                category.setCategoryType(6);
                if (txnType == "Void") {
                    Double var10001 = switchTransactionsDto.getTxnAdditionalAmount();
                    category.setDescription("Transaction Amount of INR -" + var10001 + " successful in TID: " + switchTransactionsDto.getTerminalId());
                } else {
                    Double var11 = switchTransactionsDto.getTxnAmount();
                    category.setDescription("Transaction Amount of INR " + var11 + " successful in TID: " + switchTransactionsDto.getTerminalId());
                }

                category.setUserTidVpaData(userTidVpaData);
                category.setUser(cUser);
                Category finalResponse = (Category)this.categoryRepository.save(category);
                String redirectTo = Constant.commonType(category.getCategoryType());
                this.sendPushNotificationGenerate(txnType, finalResponse.getDescription(), finalResponse, redirectTo, cUser);
            }
        } catch (Exception e) {
            logger.error("PUSHTXNNOTIFICATIONS : Exception is push settlement notification : {}", e.getMessage());
        }

    }

    @Async("asyncExecutor")
    public void pushSettlementNotifications(SettlementNotificationDto settlementNotificationDtos) {
        logger.info("Push notification is called : ------ : {}", settlementNotificationDtos);
        if (settlementNotificationDtos != null) {
            String Mid = settlementNotificationDtos.getMid();
            if (Mid != null) {
                try {
                    logger.info("Notification Data Creation Started : ---- : {}", Mid);
                    List<UserTidVpaData> userTidVpaDataList = this.userTidVpaDataRepository.findByMid(Mid);
                    if (userTidVpaDataList != null && !userTidVpaDataList.isEmpty() && userTidVpaDataList.size() != 0) {
                        UserTidVpaData userTidVpaData = (UserTidVpaData)userTidVpaDataList.get(0);
                        logger.info("Merchant details : ---- : {}", userTidVpaData);
                        User cUser = (User)this.userRepository.getOne(userTidVpaData.getUser().getId());
                        logger.info("User details : ---- : {}", cUser);
                        String fileUrl = this.fileStorageService.convertUrlToPdf(settlementNotificationDtos.getSettlementFileUrl());
                        Category finalResponse = this.saveCategoryAndSettlementData(userTidVpaData, cUser, settlementNotificationDtos, fileUrl);
                        String redirectTo = Constant.commonType(finalResponse.getCategoryType());
                        Logger var10000 = logger;
                        String var10002 = userTidVpaData.getMid();
                        var10000.info("Notification data push : title       : ---- : {}", var10002 + "_" + redirectTo);
                        logger.info("Notification data push : User        : ---- : {}", cUser.getEmail());
                        logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                        logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                        logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                        this.sendPushNotificationGenerate(userTidVpaData.getMid() + "_" + redirectTo, finalResponse.getDescription(), finalResponse, redirectTo, cUser);
                        logger.info("Notification Data Creation Completed : ---- : {}", Mid);
                    } else {
                        logger.info("The mid is not signed up with Merchant application : ------- : {}", Mid);
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
        category.setCategoryType(4);
        category.setDescription("Settlement report generation is completed for " + userTidVpaData.getMid());
        category.setUserTidVpaData(userTidVpaData);
        category.setUser(cUser);
        category.setSettlementFileUrl(settlementNotificationDtos.getSettlementFileUrl());
        category.setFileUrl(fileUrl);
        logger.info("Before category save : ------------ : {}", category);
        Category finalResponse = (Category)this.categoryRepository.save(category);
        logger.info("Category saved : -------------- : {}", finalResponse);
        SettlementRequestResponse settlementRequestResponse = this.settlementRequestResponseRepository.findByUniqueId(settlementNotificationDtos.getUniqueId());
        if (settlementRequestResponse != null) {
            settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
            logger.info("after 3 month --------- : {}", DateUtil.addThreeMonthDate());
            settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
            settlementRequestResponse.setFileExist(true);
            settlementRequestResponse.setSettlementS3Url(settlementNotificationDtos.getSettlementFileUrl());
            settlementRequestResponse.setFileUrl(fileUrl);
            SettlementRequestResponse fileResponse = (SettlementRequestResponse)this.settlementRequestResponseRepository.save(settlementRequestResponse);
            logger.info("settlement request response final ----------: {}", fileResponse);
        }

        return finalResponse;
    }

    public void pushQRSettlementNotifications(SettlementNotificationDto requestDto) {
        if (requestDto != null && requestDto.getVpa() != null) {
            try {
                logger.info("Notification Data Creation Started : ---- : {}", requestDto.getVpa());
                List<UserTidVpaData> userTidVpaDataList = this.userTidVpaDataRepository.findVpa(requestDto.getVpa());
                if (userTidVpaDataList != null && !userTidVpaDataList.isEmpty() && userTidVpaDataList.size() != 0) {
                    UserTidVpaData userTidVpaData = (UserTidVpaData)userTidVpaDataList.get(0);
                    logger.info("Merchant details : ---- : {}", userTidVpaData);
                    User cUser = (User)this.userRepository.getOne(userTidVpaData.getUser().getId());
                    logger.info("User details : ---- : {}", cUser);
                    String fileUrl = this.fileStorageService.convertUrlToPdf(requestDto.getSettlementFileUrl());
                    Category category = new Category();
                    category.setCategoryType(4);
                    category.setDescription("Settlement report generation is completed for " + userTidVpaData.getVpa());
                    category.setUserTidVpaData(userTidVpaData);
                    category.setUser(cUser);
                    category.setSettlementFileUrl(requestDto.getSettlementFileUrl());
                    category.setFileUrl(fileUrl);
                    logger.info("Before category save : ------------ : {}", category);
                    Category finalResponse = (Category)this.categoryRepository.save(category);
                    logger.info("Category saved : -------------- : {}", finalResponse);
                    SettlementRequestResponse settlementRequestResponse = this.settlementRequestResponseRepository.findByUniqueId(requestDto.getUniqueId());
                    if (settlementRequestResponse != null) {
                        settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
                        logger.info("after 3 month --------- : {}", DateUtil.addThreeMonthDate());
                        settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
                        settlementRequestResponse.setFileExist(true);
                        settlementRequestResponse.setSettlementS3Url(requestDto.getSettlementFileUrl());
                        settlementRequestResponse.setFileUrl(fileUrl);
                        SettlementRequestResponse fileResponse = (SettlementRequestResponse)this.settlementRequestResponseRepository.save(settlementRequestResponse);
                        logger.info("settlement request response final ----------: {}", fileResponse);
                    }

                    String redirectTo = Constant.commonType(category.getCategoryType());
                    Logger var10000 = logger;
                    String var10002 = userTidVpaData.getMid();
                    var10000.info("Notification data push : title       : ---- : {}", var10002 + "_" + redirectTo);
                    logger.info("Notification data push : User        : ---- : {}", cUser.getEmail());
                    logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                    logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                    logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                    this.sendPushNotificationGenerate(userTidVpaData.getMid() + "_" + redirectTo, finalResponse.getDescription(), finalResponse, redirectTo, cUser);
                    logger.info("Notification Data Creation Completed : ---- : {}", requestDto.getVpa());
                } else {
                    logger.info("The mid is not signed up with Merchant application : ------- : {}", requestDto.getVpa());
                }
            } catch (Exception e) {
                logger.error("Exception is push settlement notification : {}", e.getMessage());
            }
        }

    }

    public void insertStaticQrTxnData(AxisQrTransactionDto request) throws ParseException {
        Gson gson = new Gson();
        String requestPayload = gson.toJson(request);
        logger.info("Static Qr Txn Request Data ---{}", requestPayload);
        StaticQrTxn check = this.staticQrTxnRepository.findByAxisQrTxnBasedOnRRN(request.getRrn());
        if (check == null) {
            logger.info("enter to insert static qr txn data --{}", request.getRrn());
            StaticQrTxn staticQrTxn1 = new StaticQrTxn();
            staticQrTxn1.setCustomerVpa(request.getCustomerVpa());
            staticQrTxn1.setCreditVpa(request.getCreditVpa());
            staticQrTxn1.setMerchantId(request.getMerchantId());
            staticQrTxn1.setMerchantChannelId(request.getMerchantChannelId());
            staticQrTxn1.setMerchantTransactionId(request.getMerchantTransactionId());
            if (request.getCreditVpa().contains("kvb")) {
                DateFormat parseFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                String sDate = request.getTransactionTimestamp();
                Date date1 = parseFormat.parse(sDate);
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                sDate = displayFormat.format(date1);
                Date transDate = displayFormat.parse(sDate);
                staticQrTxn1.setCreatedAt(transDate);
                String kvbDate = DateUtil.dateToString(transDate);
                String date = DateUtil.kvbDateFormat(kvbDate);
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
            this.staticQrTxnRepository.save(staticQrTxn1);
        }

    }

    @Async("asyncExecutor")
    public void pushTxnNotificationsForAxisQr(AxisQrTransactionDto axisQrTransactionDto) {
        try {
            if (axisQrTransactionDto.getGatewayResponseCode().equals("00")) {
                UserTidVpaData qrVpaDetails = this.userTidVpaDataRepository.findByVpa(axisQrTransactionDto.getCreditVpa());
                User cUser = this.userRepository.findByUser(qrVpaDetails.getUser().getId());
                String TxnType = "QR Transaction";
                String var10000 = axisQrTransactionDto.getTransactionAmount();
                String Description = "QR Transaction Amount of INR " + var10000 + " successful in Qr vpa: " + axisQrTransactionDto.getCreditVpa();
                String redirectTo = "Transaction";
                this.sendPushNotificationGenerateForAxisQr(TxnType, Description, qrVpaDetails, redirectTo, cUser);
            }
        } catch (Exception e) {
            logger.error("PUSHTXNNOTIFICATIONS : Exception is push axis qr transaction notification : {}", e.getMessage());
        }

    }

    @Async("asyncExecutor")
    private void sendPushNotificationGenerateForAxisQr(String title, String reason, UserTidVpaData userTidVpaData, String redirectTo, User userToNotify) {
        Gson gson = new Gson();
        PushNotificationResponseDto<NotificationBodyDtoForQr> pushNotificationResponseDto = new PushNotificationResponseDto<NotificationBodyDtoForQr>();
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
        notification.setData((new Gson()).toJson(qrLeadDto));
        Notification afterNotificationSave = (Notification)this.notificationRepository.save(notification);
        qrLeadDto.setNotificationId(afterNotificationSave.getId());
        String pushData = gson.toJson(qrLeadDto);
        Map<String, String> dataElement = new HashMap();
        dataElement.put("keysandvalues", pushData);
        pushNotificationResponseDto.setData(dataElement);

        try {
            Map<String, PushNotificationResponseDto> result = new HashMap();
            result.put("message", pushNotificationResponseDto);
            String payload = gson.toJson(result);
            this.sendPushNotification(payload);
        } catch (InterruptedException | ExecutionException e) {
            ((Exception)e).printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean insertAxisMissingData(String missingDataFile) {
        boolean updated = false;
        List<StaticQrTxn> qrListOriginal = new ArrayList();
        String fileExtension = "";
        int index = missingDataFile.lastIndexOf(".");
        if (index > 0) {
            fileExtension = missingDataFile.substring(index + 1);
            if (fileExtension.equals("csv")) {
                BufferedReader fileReader = null;

                try {
                    String line = "";
                    fileReader = new BufferedReader(new FileReader(missingDataFile));
                    Files.write(Paths.get(missingDataFile), (new String(Files.readAllBytes(Paths.get(missingDataFile)))).replace("\"", "").getBytes(), new OpenOption[0]);
                    fileReader.readLine();
                    int lineNumber = 0;

                    while((line = fileReader.readLine()) != null) {
                        ++lineNumber;
                        String[] element = line.split(",");
                        if (element.length > 0 && lineNumber != 0) {
                            logger.info("Enter to insert Axis Missing Data--{}", element[10]);
                            StaticQrTxn rrnCheck = this.staticQrTxnRepository.findByAxisQrTxnBasedOnRRN(element[10]);
                            if (rrnCheck == null) {
                                StaticQrTxn staticQrTxn = new StaticQrTxn();
                                staticQrTxn.setCustomerVpa(element[0] != null ? element[0] : "");
                                staticQrTxn.setCreditVpa(element[1] != null ? element[1] : "");
                                staticQrTxn.setMerchantId(element[2] != null ? element[2] : "");
                                staticQrTxn.setMerchantChannelId(element[3] != null ? element[3] : "");
                                staticQrTxn.setMerchantTransactionId(element[4] != null ? element[4] : "");
                                String txnDateInput = element[5].replace(" ", "T");
                                String txnDate1 = DateUtil.stringToDateFroAxis(txnDateInput);
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
                    this.staticQrTxnRepository.saveAll(qrListOriginal);
                    updated = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                File file = new File(missingDataFile);
                file.delete();
                logger.info("Axis Missing Data File Removed Successfully");
                if (qrListOriginal.size() > 0) {
                    qrListOriginal.forEach((l) -> {
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

    public void downloadTxnReport(Optional<Long> from, Optional<Long> to, Optional<String> tid, UserTidVpaData userTidVpaData) throws ParseException, IOException {
        if (from.isPresent() && to.isPresent() && tid.isPresent()) {
            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
            List<Object[]> txnData = this.bijlipayTransactionRepository.fetchTidBasedTxnData(vfrom, vto, (String)tid.get());
            List<TransactionReportDto> result = new ArrayList();
            txnData.stream().forEach((m) -> {
                TransactionReportDto txnDto = new TransactionReportDto();
                txnDto.setTxnDate(m[0] == null ? "NA" : m[0].toString());
                txnDto.setMid(m[1] == null ? "NA" : m[1].toString());
                txnDto.setPayeeVpaOrTid(m[2] == null ? "NA" : m[2].toString());
                txnDto.setResponseCodeOrTxnType(m[3] == null ? "NA" : m[3].toString());
                txnDto.setBankRrn(m[4] == null ? "NA" : m[4].toString());
                String var10000 = m[7].toString();
                if (Objects.equals(Constant.getTxnType(var10000 + m[3].toString()), "Void")) {
                    txnDto.setTxnAmount(m[6] == null ? "NA" : m[6].toString());
                } else {
                    txnDto.setTxnAmount(m[5] == null ? "NA" : m[5].toString());
                }

                result.add(txnDto);
            });
            List<String> headers = Arrays.asList("TxnDate", "Mid", "Tid", "TxnType", "RRN", "TxnAmount");
            String sheetName = "Pos Transactions";
            String fileName = String.format((String)tid.get() + "_%s_%s_%s.xlsx", DateUtil.longtoDate((Long)from.get(), "yyyMMdd"), DateUtil.longtoDate((Long)to.get(), "yyyMMdd"), System.currentTimeMillis());
            String filePath = this.fileUploadDir + "/" + fileName;
            ReportUtils.posTxnExcelReport(result, headers, sheetName, filePath);
            SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
            settlementRequestResponse.setUser(userTidVpaData.getUser());
            settlementRequestResponse.setFromDate(DateUtil.longtoDate((Long)from.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setToDate(DateUtil.longtoDate((Long)to.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setTid((String)tid.get());
            settlementRequestResponse.setMid(userTidVpaData.getMid());
            settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
            logger.info("after 3 month --------- : {}", DateUtil.addThreeMonthDate());
            settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
            settlementRequestResponse.setFileExist(true);
            settlementRequestResponse.setFileUrl(this.fileDownloadUrl + fileName);
            settlementRequestResponse.setCategoryType("Transaction");
            SettlementRequestResponse fileResponse = (SettlementRequestResponse)this.settlementRequestResponseRepository.save(settlementRequestResponse);
            logger.info("settlement request response final ----------: {}", fileResponse);
        }

    }

    public void downloadQrReport(Optional<Long> from, Optional<Long> to, Optional<String> vpa) throws ParseException {
        if (from.isPresent() && to.isPresent()) {
            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
            List<Object[]> qrTxnData = this.staticQrTxnRepository.fetchTidBasedTxnData(vfrom, vto, (String)vpa.get());
            List<AxisQrTransactionDto> result = new ArrayList();
            qrTxnData.stream().forEach((m) -> {
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
            String fileName = String.format("QrTransactionReport_%s_%s_%s.xlsx", DateUtil.longtoDate((Long)from.get(), "yyyMMdd"), DateUtil.longtoDate((Long)to.get(), "yyyMMdd"), System.currentTimeMillis());
            String filePath = this.fileUploadDir + "/" + fileName;
            ReportUtils.QrTxnReport(result, headers, sheetName, filePath);
            SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
            settlementRequestResponse.setUser(this.userRepository.findByUser(this.getSessionUser().getId()));
            settlementRequestResponse.setFromDate(DateUtil.longtoDate((Long)from.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setToDate(DateUtil.longtoDate((Long)to.get(), "dd-MM-yyyy"));
            settlementRequestResponse.setVpa((String)vpa.get());
            settlementRequestResponse.setAvailableDate(DateUtil.currentDate());
            logger.info("after 3 month --------- : {}", DateUtil.addThreeMonthDate());
            settlementRequestResponse.setExpiryDate(DateUtil.addThreeMonthDate());
            settlementRequestResponse.setFileExist(true);
            settlementRequestResponse.setFileUrl(this.fileDownloadUrl + fileName);
            settlementRequestResponse.setCategoryType("Transaction");
            SettlementRequestResponse fileResponse = (SettlementRequestResponse)this.settlementRequestResponseRepository.save(settlementRequestResponse);
            logger.info("settlement request response final ----------: {}", fileResponse);
        }

    }

    public static void QrTransactionExcelReport(List<AxisQrTransactionDto> result, List<String> headers, String sheetName, String filePath) {
        SXSSFWorkbook wb = new SXSSFWorkbook();
        Sheet sh = wb.createSheet(sheetName);
        Row headerRow = sh.createRow(0);

        for(int i = 0; i < headers.size(); ++i) {
            headerRow.createCell(i).setCellValue((String)headers.get(i));
        }

        int startRow = 1;

        for(AxisQrTransactionDto transaction : result) {
            Row shRow = sh.createRow(startRow++);
            List<String> rowAsString = transaction.getAsList();

            for(int cellnum = 0; cellnum < rowAsString.size(); ++cellnum) {
                shRow.createCell(cellnum).setCellValue((String)rowAsString.get(cellnum));
            }
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);

            try {
                wb.write(fileOut);
                wb.close();
                System.out.println("Excel file written successfully to: " + filePath);
            } catch (Throwable var14) {
                try {
                    fileOut.close();
                } catch (Throwable var13) {
                    var14.addSuppressed(var13);
                }

                throw var14;
            }

            fileOut.close();
        } catch (IOException e) {
            System.err.println("Error writing Excel file: " + e.getMessage());
        }

    }

    public static void posTransactionExcelReport(List<TransactionReportDto> result, List<String> headers, String sheetName, String filePath) {
        SXSSFWorkbook wb = new SXSSFWorkbook();
        Sheet sh = wb.createSheet(sheetName);
        Row headerRow = sh.createRow(0);

        for(int i = 0; i < headers.size(); ++i) {
            headerRow.createCell(i).setCellValue((String)headers.get(i));
        }

        int startRow = 1;

        for(TransactionReportDto transaction : result) {
            Row shRow = sh.createRow(startRow++);
            List<String> rowAsString = transaction.getAsList();

            for(int cellnum = 0; cellnum < rowAsString.size(); ++cellnum) {
                shRow.createCell(cellnum).setCellValue((String)rowAsString.get(cellnum));
            }
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);

            try {
                wb.write(fileOut);
                wb.close();
                System.out.println("Excel file written successfully to: " + filePath);
            } catch (Throwable var14) {
                try {
                    fileOut.close();
                } catch (Throwable var13) {
                    var14.addSuppressed(var13);
                }

                throw var14;
            }

            fileOut.close();
        } catch (IOException e) {
            System.err.println("Error writing Excel file: " + e.getMessage());
        }

    }

    @Async("asyncExecutor")
    public void pushSettlementNotificationsForToday(SettlementNotificationUpdate settlementNotificationDtos) {
        logger.info("Push notification is called : ------ : {}", settlementNotificationDtos);
        if (settlementNotificationDtos != null) {
            String[] Mid = settlementNotificationDtos.getMid();
            if (Mid.length > 0) {
                try {
                    for(String mid : Mid) {
                        logger.info("Notification Data Creation Started : ---- : {}", mid);
                        List<UserTidVpaData> merchantDeatilsList = this.userTidVpaDataRepository.findByMid(mid);
                        if (merchantDeatilsList != null && !merchantDeatilsList.isEmpty() && merchantDeatilsList.size() != 0) {
                            UserTidVpaData merchantDetails = (UserTidVpaData)merchantDeatilsList.get(0);
                            logger.info("Merchant details : ---- : {}", merchantDetails);
                            User cUser = (User)this.userRepository.getOne(merchantDetails.getUser().getId());
                            logger.info("User details : ---- : {}", cUser);
                            Category category = new Category();
                            category.setCategoryType(4);
                            category.setDescription("Settlement for today is completed for " + merchantDetails.getMid());
                            category.setUserTidVpaData(merchantDetails);
                            category.setUser(cUser);
                            logger.info("Before category save : ------------ : {}", category);
                            Category finalResponse = (Category)this.categoryRepository.save(category);
                            logger.info("Category saved : -------------- : {}", finalResponse);
                            String redirectTo = Constant.commonType(category.getCategoryType());
                            Logger var10000 = logger;
                            String var10002 = merchantDetails.getMid();
                            var10000.info("Notification data push : title       : ---- : {}", var10002 + "_" + redirectTo);
                            logger.info("Notification data push : User        : ---- : {}", cUser.getEmail());
                            logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                            logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                            logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                            this.sendPushNotificationGenerate(merchantDetails.getMid() + "_" + redirectTo, finalResponse.getDescription(), finalResponse, redirectTo, cUser);
                            logger.info("Notification Data Creation Completed : ---- : {}", mid);
                        } else {
                            logger.info("The mid is not signed up with Merchant application : ------- : {}", mid);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception is push settlement notification : {}", e.getMessage());
                }
            }
        }

    }

    public void insertAnteraPaytivoTxn(List<Object[]> txnData) {
        logger.info("Antera Paytivo txn data Size --{}", txnData.size());
        List<BijlipayTransaction> transactions = new ArrayList();
        if (txnData.size() > 0) {
            txnData.forEach((o) -> {
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
                    bijlipayTransaction.setTxnAmount((double)0.0F);
                    ++count;
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
                    this.bijlipayTransactionRepository.saveAll(transactions);
                    logger.info("Missing SWITCH TXN - Saved in to the DB : ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public double getAmount(Page<SwitchTransactionsDto> response) {
        return response.getContent().stream().filter((l) -> "00".equalsIgnoreCase(l.getTxnResponseCode()) && !"36".equalsIgnoreCase(l.getTxnType()) && !"38".equalsIgnoreCase(l.getTxnType()) && !"48".equalsIgnoreCase(l.getTxnType())).mapToDouble(SwitchTransactionsDto::getTxnAmount).sum();
    }

    public double getQrAmount(Page<AxisQrTransactionDto> response) {
        return response.getContent().stream().mapToDouble((s) -> Double.parseDouble(s.getTransactionAmount())).sum();
    }

    public Double getAxisQrTransactionDetailsAmount(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<List<String>> vpa) throws ParseException {
        if (action.isPresent()) {
            switch ((Integer)value.get()) {
                case 1:
                    return this.staticQrTxnRepository.findAxisQrTxnThisMonthDataSum((List)vpa.orElse(vpaDetails));
                case 2:
                    return this.staticQrTxnRepository.findAxisQrTxnLast30DaysDataAmount((List)vpa.orElse(vpaDetails));
                case 3:
                    if (vpa.isPresent()) {
                        if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", new Object[]{vfrom, vto, vpaDetails.get(0), searchTerm.get()});
                            return this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTermAmount(vfrom, vto, (List)vpa.get(), (String)searchTerm.get());
                        } else if (from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", new Object[]{vfrom, vto, vpaDetails.get(0)});
                            return this.staticQrTxnRepository.findAxisQrBasedOnVpaWithDatesAmount(vfrom, vto, (List)vpa.get());
                        } else if (searchTerm.isPresent()) {
                            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                            return this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTermAmount((List)vpa.get(), (String)searchTerm.get());
                        }
                    } else if (from.isPresent() && to.isPresent() && searchTerm.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : Search Term : --------- : {}", new Object[]{vfrom, vto, vpaDetails.get(0), searchTerm.get()});
                        return this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithDatesAndSearchTermAmount(vfrom, vto, Collections.singletonList((String)vpaDetails.get(0)), (String)searchTerm.get());
                    } else if (from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        logger.info("Axis Qr Txn : The from Date : {} ------------ To Date : {} ------------ VPA : {} : ", new Object[]{vfrom, vto, vpaDetails.get(0)});
                        return this.staticQrTxnRepository.findAxisQrBasedOnVpaWithDatesAmount(vfrom, vto, Collections.singletonList((String)vpaDetails.get(0)));
                    } else if (searchTerm.isPresent()) {
                        logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
                        return this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTermAmount(Collections.singletonList((String)vpaDetails.get(0)), (String)searchTerm.get());
                    }
                default:
                    logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
                    return this.staticQrTxnRepository.findAxisQrTxnAmount(Collections.singletonList((String)vpaDetails.get(0)));
            }
        } else if (searchTerm.isPresent()) {
            logger.info("Axis Qr Txn : VPA : {} : Search Term : --------- : {}", vpaDetails.get(0), searchTerm.get());
            return this.staticQrTxnRepository.findAxisQrTxnBasedOnVpaWithSearchTermAmount((List)vpa.orElseGet(() -> Collections.singletonList((String)vpaDetails.get(0))), (String)searchTerm.get());
        } else {
            logger.info("Axis Qr Txn : VPA : {} ", vpaDetails.get(0));
            return this.staticQrTxnRepository.findAxisQrTxnAmount((List)vpa.orElseGet(() -> Collections.singletonList((String)vpaDetails.get(0))));
        }
    }

    public Double getSwitchTransactionsSearchAmount(Optional<Integer> action, List<String> tidList, Optional<List<String>> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm) throws ParseException {
        Double amount = null;
        if (!action.isPresent()) {
            if (searchTerm.isPresent()) {
                logger.info("WITHOUT ACTION------------------------:");
                return this.bijlipayTransactionRepository.findAllTransactionDataWithSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get());
            } else {
                logger.info("WITHOUT ACTION------------------------:");
                return this.bijlipayTransactionRepository.findAllTransactionDataAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))));
            }
        } else {
            if ((Integer)action.get() == 1) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get());
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return amount;
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), (String)searchTerm.get());
                        }
                    } else {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusSuccessAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))));
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return amount;
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnStatusBothSuccessFailureAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))));
                        }
                    }
                }
            } else if ((Integer)action.get() == 2) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (!requestDto.getTxnType().isEmpty()) {
                    if (searchTerm.isPresent()) {
                        return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeWithSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get());
                    }

                    return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnTypeAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType());
                }
            } else if ((Integer)action.get() == 3) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getDateRange() != 0) {
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return this.bijlipayTransactionRepository.findTransactionDataWithDateRange3WithSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get());
                        }
                    } else if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        return this.bijlipayTransactionRepository.findTransactionDataWithDateRange3Amount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto);
                    }
                }
            } else if ((Integer)action.get() == 4) {
                logger.info("ACTION ------------------------: {}", action.get());
                if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ALL ACTION ------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            if (requestDto.getTxnStatus() == 1) {
                                return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get());
                            }

                            if (requestDto.getTxnStatus() == 2) {
                                return amount;
                            }

                            if (requestDto.getTxnStatus() == 3) {
                                return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get());
                            }
                        }
                    } else if (requestDto.getDateRange() == 3) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        if (requestDto.getTxnStatus() == 1) {
                            return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessDateRange3Amount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto);
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return amount;
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithBothSuccessFailureDateRange3Amount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto);
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && !requestDto.getTxnType().isEmpty()) {
                    logger.info(" ACTION txn status and txn type------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get());
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return amount;
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), (String)searchTerm.get());
                        }
                    } else {
                        if (requestDto.getTxnStatus() == 1) {
                            return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessAndTypeAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType());
                        }

                        if (requestDto.getTxnStatus() == 2) {
                            return amount;
                        }

                        if (requestDto.getTxnStatus() == 3) {
                            return this.bijlipayTransactionRepository.findTransactionDataAllFilterWithTxnSuccessFailureAndTypeAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType());
                        }
                    }
                } else if (requestDto.getTxnStatus() != 0 && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn status and date range------------------------: {}", action.get());
                    String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                    String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 1) {
                            return this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3WithSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get());
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 2) {
                            return amount;
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 3) {
                            return this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto, (String)searchTerm.get());
                        }
                    } else {
                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 1) {
                            return this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessAndDateRange3Amount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto);
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 2) {
                            return amount;
                        }

                        if (requestDto.getDateRange() == 3 && requestDto.getTxnStatus() == 3) {
                            return this.bijlipayTransactionRepository.findTransactionDataWithTxnSuccessFailureAndDateRange3Amount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), vfrom, vto);
                        }
                    }
                } else if (!requestDto.getTxnType().isEmpty() && requestDto.getDateRange() != 0) {
                    logger.info(" ACTION txn type and date range------------------------: {}", action.get());
                    if (searchTerm.isPresent()) {
                        if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                            String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                            String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                            return this.bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3WithSearchAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto, (String)searchTerm.get());
                        }
                    } else if (requestDto.getDateRange() == 3 && from.isPresent() && to.isPresent()) {
                        String vfrom = DateUtil.longtoDate((Long)from.get(), "yyyy-MM-dd").concat(" 00:00:00");
                        String vto = DateUtil.longtoDate((Long)to.get(), "yyyy-MM-dd").concat(" 23:59:59");
                        return this.bijlipayTransactionRepository.findTransactionDataWithTxnTypeAndDateRange3Amount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))), requestDto.getTxnType(), vfrom, vto);
                    }
                }
            }

            return this.bijlipayTransactionRepository.findAllTransactionDataAmount((List)tid.orElseGet(() -> Collections.singletonList((String)tidList.get(0))));
        }
    }

    @Transactional
    public void store(JupiterTxnData jupiterTxnData) throws ParseException {
        BijlipayTransaction txnData = new BijlipayTransaction();
        txnData.setMti(jupiterTxnData.getMti() != null ? jupiterTxnData.getMti() : "");
        txnData.setTxnType(jupiterTxnData.getTxnType() != null ? jupiterTxnData.getTxnType() : "");
        txnData.setTerminalId(jupiterTxnData.getTerminalId() != null ? jupiterTxnData.getTerminalId() : "");
        txnData.setMerchantId(jupiterTxnData.getMerchantId() != null ? jupiterTxnData.getMerchantId() : "");
        txnData.setTxnDate(jupiterTxnData.getTxnDate() != null ? jupiterTxnData.getTxnDate() : "");
        txnData.setTxnTime(jupiterTxnData.getTxnTime() != null ? jupiterTxnData.getTxnTime() : "");
        if (jupiterTxnData.getTxnAmount() != null) {
            String amt = jupiterTxnData.getTxnAmount();
            long parseAmt = Long.parseLong(amt);
            Double txnAmt = (double)parseAmt / (double)100.0F;
            String formattedAmt = String.format("%.2f", txnAmt);
            txnData.setTxnAmount(formattedAmt);
            logger.info("amt {} parsed Amt {}", amt, formattedAmt);
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
        if (!jupiterTxnData.getTxnAdditionalAmount().isEmpty()) {
            String amt = jupiterTxnData.getTxnAdditionalAmount();
            long parseAmt = Long.parseLong(amt);
            Double txnAmt = (double)parseAmt / (double)100.0F;
            String formattedAmt = String.format("%.2f", txnAmt);
            txnData.setTxnAdditionalAmount(formattedAmt);
            logger.info("amt {} parsed Amt {}", amt, formattedAmt);
        }

        txnData.setMaskedcardnumber(jupiterTxnData.getMaskedCardNumber() != null ? jupiterTxnData.getMaskedCardNumber() : "");
        txnData.setAuthCode(jupiterTxnData.getAuthCode() != null ? jupiterTxnData.getAuthCode() : "null");
        this.txnRepository.save(txnData);
    }
}