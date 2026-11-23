package com.bijlipay.service;

import com.bijlipay.model.Invoice;
import com.bijlipay.model.UserTidVpaData;
import com.bijlipay.model.dto.AxisQrTransactionDto;
import com.bijlipay.model.dto.JupiterTxnData;
import com.bijlipay.model.dto.SettlementNotificationDto;
import com.bijlipay.model.dto.SwitchTransactionsDto;
import com.bijlipay.model.dto.TransactionRequestDto;
import com.bijlipay.model.dto.notification.SettlementNotificationUpdate;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import javax.mail.MessagingException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BijlipayTransactionService {
    List<Object[]> getSumOfTodayAndYesterdayVoidAndReversalTxnAmount(String tid, long sessionUserId);

    List<SwitchTransactionsDto> getLastFiveSwicthTransactions(String tid) throws IOException;

    List<AxisQrTransactionDto> getLastFiveAxisQrTransactions(String vpaDetails) throws SQLException;

    List<Object[]> getSumAndCountOfTodayAndYesterdayTxnAmountForTid(String tid, long sessionUserId);

    List<Object[]> getSumAndCountOfTodayAndYesterdayTxnAmountForVpa(String vpa, long sessionUserId);

    Page<AxisQrTransactionDto> getAxisQrTransactionDetails(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<List<String>> vpa, Pageable pageable) throws ParseException;

    Page<SwitchTransactionsDto> getSwitchTransactionsSearch(Optional<Integer> action, List<String> tidList, Optional<List<String>> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm, Pageable pageable) throws ParseException;

    Page<SwitchTransactionsDto> getSwitchTransactionsSearchData(Optional<Integer> action, List<String> tidList, Optional<List<String>> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm, Pageable pageable) throws ParseException;

    InputStreamResource downloadTransactionReport(Optional<Integer> action, List<String> tidList, Optional<String> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm) throws IOException, ParseException;

    InputStreamResource downloadQrTransactionReport(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<String> vpa) throws ParseException;

    InputStreamResource downloadQrTransactionReportData(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<String> vpa) throws ParseException;

    void requestMailForInvoiceCreation(Invoice invoice) throws MessagingException;

    void pushTxnNotifications(SwitchTransactionsDto switchTransactionsDto);

    void pushSettlementNotifications(SettlementNotificationDto requestDto);

    void pushQRSettlementNotifications(SettlementNotificationDto requestDto);

    void insertStaticQrTxnData(AxisQrTransactionDto axisQrTransactionDto) throws ParseException;

    void pushTxnNotificationsForAxisQr(AxisQrTransactionDto axisQrTransactionDto);

    boolean insertAxisMissingData(String missingDataFile);

    List<SwitchTransactionsDto> getLastFiveSwicthTransactionsBasedOnUser(List<String> tidList);

    List<SwitchTransactionsDto> getLastFiveSwicthTransactionsBasedOnUserPortal(List<String> tidList);

    List<AxisQrTransactionDto> getLastFiveAxisQrTransactionsBasedOnUser(List<String> vpaList);

    List<AxisQrTransactionDto> getLastFiveAxisQrTransactionsBasedOnUserPortal(List<String> vpaList);

    void downloadTxnReport(Optional<Long> from, Optional<Long> to, Optional<String> tid, UserTidVpaData userTidVpaData) throws ParseException, IOException;

    void downloadQrReport(Optional<Long> from, Optional<Long> to, Optional<String> vpa) throws ParseException;

    void pushSettlementNotificationsForToday(SettlementNotificationUpdate requestDto);

    void insertAnteraPaytivoTxn(List<Object[]> txnData);

    double getAmount(Page<SwitchTransactionsDto> response);

    double getQrAmount(Page<AxisQrTransactionDto> response);

    Double getAxisQrTransactionDetailsAmount(Optional<Integer> action, Optional<Integer> value, Optional<String> searchTerm, Optional<Long> from, Optional<Long> to, List<String> vpaDetails, Optional<List<String>> vpa) throws ParseException;

    Double getSwitchTransactionsSearchAmount(Optional<Integer> action, List<String> tidData, Optional<List<String>> tid, TransactionRequestDto requestDto, Optional<Long> from, Optional<Long> to, Optional<String> searchTerm) throws ParseException;

    void store(JupiterTxnData jupiterTxnData) throws ParseException;
}