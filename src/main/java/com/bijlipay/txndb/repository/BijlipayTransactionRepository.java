package com.bijlipay.txndb.repository;

import com.bijlipay.txndb.model.BijlipayTransaction;
import java.util.List;
import javax.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED
)
public interface BijlipayTransactionRepository extends JpaRepository<BijlipayTransaction, Long> {
    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime, rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode, bin as bin,customer_vpa as customerVpa  FROM bijlipay_transaction  WHERE terminal_id=?1 AND response_received_time >= CURDATE() - INTERVAL 30 DAY AND txn_type NOT IN ('36') AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id = ?1 AND b2.response_received_time >= CURDATE() - INTERVAL 30 DAY GROUP BY b2.terminal_id, b2.stan)) order by response_received_time desc LIMIT 5",
            nativeQuery = true
    )
    List<Tuple> findLastFiveSwitchTransaction(String tid);

    @Query(
            value = "SELECT SUM(txn_amount) as txnamount,convert(varchar,response_received_time,23),count() as date FROM bijlipayTransaction   WHERE terminal_id=?1 AND txn_response_code ='00' AND txn_amount > 0 AND ((mti IN ('0200', '0400') AND txn_type IN ('00', '09')) OR (mti IN ('0100') AND txn_type IN ('00', '37', '39'))) Group BY convert(varchar,response_received_time,23)  ORDER BY convert(varchar,response_received_time,23) DESC",
            nativeQuery = true
    )
    List<Object[]> fetchSumOfTxnAmountSwitchTxn(String tid);

    @Query(
            value = "SELECT SUM(CASE WHEN DATE(response_received_time) = CURDATE() THEN txn_additional_amount ELSE 0 END) AS todayVoidReversalSum,COUNT(CASE WHEN DATE(response_received_time) = CURDATE() THEN 1 ELSE NULL END) AS todayVoidReversalCount,SUM(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN txn_additional_amount ELSE 0 END) AS yesterdayVoidReversalSum,COUNT(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN 1 ELSE NULL END) AS yesterdayVoidReversalCount  FROM bijlipay_transaction WHERE terminal_id =?1 AND txn_additional_amount IS NOT NULL  AND response_received_time >= CURDATE() - INTERVAL 1 DAY  AND txn_response_code ='00' AND ((mti = '0200' AND txn_type = '02') OR (mti = '0400'))",
            nativeQuery = true
    )
    List<Object[]> fetchSumOfVoidReversalTxnAmountSwitchTxn(String tid);

    @Query(
            value = "SELECT SUM(CASE WHEN DATE(response_received_time) = CURDATE() THEN txn_amount ELSE 0 END) AS today_sum,COUNT(CASE WHEN DATE(response_received_time) = CURDATE() THEN 1 ELSE NULL END) AS today_count,SUM(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN txn_amount ELSE 0 END) AS yesterday_sum,COUNT(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN 1 ELSE NULL END) AS yesterday_count  FROM bijlipay_transaction WHERE terminal_id=?1 AND  mti ='0200'and txn_response_code ='00' and txn_type ='00' AND txn_amount > 0",
            nativeQuery = true
    )
    Object[] fetchSumAndCountOfTodayAndYesterdayTxn200(String tid);

    @Query(
            value = "SELECT SUM(CASE WHEN DATE(response_received_time) = CURDATE() THEN txn_additional_amount ELSE 0 END) AS today_sum,COUNT(CASE WHEN DATE(response_received_time) = CURDATE() THEN 1 ELSE NULL END) AS today_count,SUM(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN txn_additional_amount ELSE 0 END) AS yesterday_sum,COUNT(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN 1 ELSE NULL END) AS yesterday_count  FROM bijlipay_transaction WHERE terminal_id=?1 AND  mti ='0200'and txn_response_code ='00' and txn_type ='02' AND txn_additional_amount > 0",
            nativeQuery = true
    )
    Object[] fetchSumAndCountOfTodayAndYesterdayTxn200Void(String tid);

    @Query(
            value = "SELECT SUM(CASE WHEN DATE(response_received_time) = CURDATE() THEN txn_amount ELSE 0 END) AS today_sum, COUNT(CASE WHEN DATE(response_received_time) = CURDATE() THEN 1 ELSE NULL END) AS today_count, SUM(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN txn_amount ELSE 0 END) AS yesterday_sum, COUNT(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN 1 ELSE NULL END) AS yesterday_count FROM bijlipay_transaction WHERE terminal_id=?1 AND  mti ='0400' and txn_response_code ='00' and txn_type ='00' AND txn_amount > 0",
            nativeQuery = true
    )
    Object[] fetchSumAndCountOfTodayAndYesterdayTxn400(String tid);

    @Query(
            value = "SELECT SUM(CASE WHEN DATE(response_received_time) = CURDATE() THEN txn_amount ELSE 0 END) AS today_sum, COUNT(CASE WHEN DATE(response_received_time) = CURDATE() THEN 1 ELSE NULL END) AS today_count, SUM(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN txn_amount ELSE 0 END) AS yesterday_sum, COUNT(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN 1 ELSE NULL END) AS yesterday_count FROM bijlipay_transaction WHERE terminal_id=?1 AND  mti ='0100' and txn_response_code ='00'  and txn_type ='37' AND txn_amount > 0 AND transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id = ?1 GROUP BY b2.terminal_id, b2.stan)",
            nativeQuery = true
    )
    Object[] fetchSumAndCountOfTodayAndYesterdayTxn10037(String tid);

    @Query(
            value = "SELECT SUM(CASE WHEN DATE(response_received_time) = CURDATE() THEN txn_amount ELSE 0 END) AS today_sum, COUNT(CASE WHEN DATE(response_received_time) = CURDATE() THEN 1 ELSE NULL END) AS today_count, SUM(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN txn_amount ELSE 0 END) AS yesterday_sum, COUNT(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN 1 ELSE NULL END) AS yesterday_count  FROM bijlipay_transaction WHERE terminal_id=?1 AND  mti ='0100' and txn_response_code ='00'  and txn_type ='39' AND txn_amount > 0 AND transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id = ?1 GROUP BY b2.terminal_id, b2.stan)",
            nativeQuery = true
    )
    Object[] fetchSumAndCountOfTodayAndYesterdayTxn10039(String tid);

    @Query(
            value = "SELECT mti, txn_type, SUM(CASE WHEN DATE(response_received_time) = CURDATE() THEN COALESCE(IF(mti='0200' AND txn_type='02', txn_additional_amount, txn_amount), 0) ELSE 0 END) AS today_sum, COUNT(CASE WHEN DATE(response_received_time) = CURDATE() THEN 1 ELSE NULL END) AS today_count, SUM(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN COALESCE(IF(mti='0200' AND txn_type='02', txn_additional_amount, txn_amount), 0) ELSE 0 END) AS yesterday_sum, COUNT(CASE WHEN DATE(response_received_time) = CURDATE() - INTERVAL 1 DAY THEN 1 ELSE NULL END) AS yesterday_count FROM bijlipay_transaction WHERE terminal_id = ?1 AND txn_response_code = '00' AND response_received_time >= CURDATE() - INTERVAL 1 DAY AND ( (mti = '0200' AND txn_type = '00' AND txn_amount > 0)    OR (mti = '0200' AND txn_type = '02' AND txn_additional_amount > 0)    OR (mti = '0400' AND txn_type = '00' AND txn_amount > 0)    OR (mti = '0100' AND txn_type IN ('37','39') AND txn_amount > 0        AND transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id = ?1 AND b2.response_received_time >= CURDATE() - INTERVAL 1 DAY GROUP BY b2.terminal_id, b2.stan)) ) GROUP BY mti, txn_type",
            nativeQuery = true
    )
    List<Object[]> fetchConsolidatedTodayYesterdaySums(String tid);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnSuccessDateRange3(List<String> tid, List<String> txnType, String aLong, String aLong1, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnSuccessDateRange3Amount(List<String> tid, List<String> txnType, String aLong, String aLong1);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnSuccessDateRange3(String tid, List<String> txnType, String aLong, String aLong1);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa   FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5%  OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5%  OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch(List<String> tid, List<String> txnType, String aLong, String aLong1, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5%  OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearchAmount(List<String> tid, List<String> txnType, String aLong, String aLong1, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnSuccessDateRange3WithSearch(String tid, List<String> txnType, String aLong, String aLong1, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureDateRange3(List<String> tid, List<String> txnType, String aLong, String aLong1, Pageable pageable);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100'))  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100'))  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureDateRange3848(List<String> tid, List<String> txnType, String aLong, String aLong1, Pageable pageable);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100'))  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureDateRange3848(String tid, List<String> txnType, String aLong, String aLong1);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_type IN (?2) AND bt.mti = '0100')  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_type IN (?2) AND bt.mti = '0100')  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848(List<String> tid, List<String> txnType, String aLong, String aLong1, Pageable pageable);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND (bt.txn_type IN (?2) AND bt.mti = '0100')  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureDateRangeOnly3848(String tid, List<String> txnType, String aLong, String aLong1);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureDateRange3(String tid, List<String> txnType, String aLong, String aLong1);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa   FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch(List<String> tid, List<String> txnType, String aLong, String aLong1, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchAmount(List<String> tid, List<String> txnType, String aLong, String aLong1, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa   FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100'))  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100'))  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848(List<String> tid, List<String> txnType, String aLong, String aLong1, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100'))  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500')  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848Amount(List<String> tid, List<String> txnType, String aLong, String aLong1, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100'))  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch3848(String tid, List<String> txnType, String aLong, String aLong1, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa   FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_type IN (?2) AND bt.mti = '0100')  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4)  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_type IN (?2) AND bt.mti = '0100')  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4)  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848(List<String> tid, List<String> txnType, String aLong, String aLong1, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_type IN (?2) AND bt.mti = '0100')  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4)  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848Amount(List<String> tid, List<String> txnType, String aLong, String aLong1, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti   FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND (bt.txn_type IN (?2) AND bt.mti = '0100')  AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4)  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureDateRange3WithSearchOnly3848(String tid, List<String> txnType, String aLong, String aLong1, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureDateRange3WithSearch(String tid, List<String> txnType, String aLong, String aLong1, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithBothSuccessFailureDateRange3(List<String> tid, List<String> txnType, String aLong, String aLong1, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithBothSuccessFailureDateRange3Amount(List<String> tid, List<String> txnType, String aLong, String aLong1);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithBothSuccessFailureDateRange3(String tid, List<String> txnType, String aLong, String aLong1);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa   FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))   ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))   ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch(List<String> tid, List<String> txnType, String aLong, String aLong1, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))   ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearchAmount(List<String> tid, List<String> txnType, String aLong, String aLong1, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithBothSuccessFailureDateRange3WithSearch(String tid, List<String> txnType, String aLong, String aLong1, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa   FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnSuccessAndType(List<String> tid, List<String> txnType, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnSuccessAndTypeAmount(List<String> tid, List<String> txnType);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnSuccessAndType(String tid, List<String> txnType);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa   FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch(List<String> tid, List<String> txnType, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearchAmount(List<String> tid, List<String> txnType, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code ='00' AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnSuccessAndTypeWithSearch(String tid, List<String> txnType, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT  count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureAndType(List<String> tid, List<String> txnType, Pageable pageable);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100')) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT  count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100')) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureAndType3848(List<String> tid, List<String> txnType, Pageable pageable);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100')) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureAndType3848(String tid, List<String> txnType);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_type IN (?2) AND bt.mti = '0100') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT  count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_type IN (?2) AND bt.mti = '0100') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848(List<String> tid, List<String> txnType, Pageable pageable);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND (bt.txn_type IN (?2) AND bt.mti = '0100') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureAndTypeOnly3848(String tid, List<String> txnType);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureAndType(String tid, List<String> txnType);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch(List<String> tid, List<String> txnType, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100')) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100')) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848(List<String> tid, List<String> txnType, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti   FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND ((bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2)) OR (bt.txn_type IN ('38','48') AND bt.mti = '0100')) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3%  OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch3848(String tid, List<String> txnType, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND (bt.txn_type IN (?2) AND bt.mti = '0100') AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND (bt.txn_type IN (?2) AND bt.mti = '0100') AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848(List<String> tid, List<String> txnType, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND (bt.txn_type IN (?2) AND bt.mti = '0100') AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureAndTypeWithSearchOnly3848(String tid, List<String> txnType, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code NOT IN ('00') AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnFailureAndTypeWithSearch(String tid, List<String> txnType, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnSuccessFailureAndType(List<String> tid, List<String> txnType, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnSuccessFailureAndTypeAmount(List<String> tid, List<String> txnType);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnSuccessFailureAndType(String tid, List<String> txnType);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa   FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3%) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch(List<String> tid, List<String> txnType, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearchAmount(List<String> tid, List<String> txnType, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnSuccessFailureAndTypeWithSearch(String tid, List<String> txnType, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00'  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00'  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataWithTxnSuccessAndDateRange3(List<String> tid, String fromDate, String toDate, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00'  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            nativeQuery = true
    )
    Double findTransactionDataWithTxnSuccessAndDateRange3Amount(List<String> tid, String fromDate, String toDate);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code ='00'  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataWithTxnSuccessAndDateRange3(String tid, String fromDate, String toDate);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00'  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00'  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataWithTxnSuccessAndDateRange3WithSearch(List<String> tid, String fromDate, String toDate, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00'  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Double findTransactionDataWithTxnSuccessAndDateRange3WithSearchAmount(List<String> tid, String fromDate, String toDate, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code ='00'  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500')  AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataWithTxnSuccessAndDateRange3WithSearch(String tid, String fromDate, String toDate, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100'))  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3)  AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            countQuery = "SELECT COUNT(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100'))  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3)  AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataWithTxnFailureAndDateRange3(List<String> tid, String fromDate, String toDate, Pageable pageable);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code NOT IN ('00') AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time desc",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataWithTxnFailureAndDateRange3(String tid, String fromDate, String toDate);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100'))  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100')) AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataWithTxnFailureAndDateRange3WithSearch(List<String> tid, String fromDate, String toDate, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code NOT IN ('00') AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% ) AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataWithTxnFailureAndDateRange3WithSearch(String tid, String fromDate, String toDate, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)   AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)   AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataWithTxnSuccessFailureAndDateRange3(List<String> tid, String fromDate, String toDate, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00'  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 )",
            nativeQuery = true
    )
    Double findTransactionDataWithTxnSuccessFailureAndDateRange3Amount(List<String> tid, String fromDate, String toDate);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataWithTxnSuccessFailureAndDateRange3(String tid, String fromDate, String toDate);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)   AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% )  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)   AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% )  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch(List<String> tid, String fromDate, String toDate, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code ='00' AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% )  ",
            nativeQuery = true
    )
    Double findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearchAmount(List<String> tid, String fromDate, String toDate, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% ) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataWithTxnSuccessFailureAndDateRange3WithSearch(String tid, String fromDate, String toDate, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            countQuery = "SELECT COUNT(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataWithTxnTypeAndDateRange3(List<String> tid, List<String> txnType, String vfrom, String vto, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            nativeQuery = true
    )
    Double findTransactionDataWithTxnTypeAndDateRange3Amount(List<String> tid, List<String> txnType, String vfrom, String vto);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataWithTxnTypeAndDateRange3(String tid, List<String> txnType, String vfrom, String vto);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS  txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% )   ",
            countQuery = "SELECT COUNT(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% )   ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataWithTxnTypeAndDateRange3WithSearch(List<String> tid, List<String> txnType, String vfrom, String vto, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount)FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% )   ",
            nativeQuery = true
    )
    Double findTransactionDataWithTxnTypeAndDateRange3WithSearchAmount(List<String> tid, List<String> txnType, String vfrom, String vto, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_type IN (?2) AND (bt.response_received_time >=?3 AND bt.response_received_time <=?4) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?5% OR mti LIKE ?5% AND txn_type LIKE ?5% OR txn_amount LIKE ?5% OR terminal_id LIKE ?5% OR merchant_id LIKE ?5% OR bt.txn_additional_amount LIKE ?5% OR bt.invoice_number LIKE ?5% OR bt.batch_number LIKE ?5% OR bt.rrn_number LIKE ?5% )  order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataWithTxnTypeAndDateRange3WithSearch(String tid, List<String> txnType, String vfrom, String vto, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code = '00' AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code = '00' AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnStatusSuccess(List<String> tid, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code = '00' AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 )  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnStatusSuccessAmount(List<String> tid);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code = '00' AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnStatusSuccess(String tid);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100'))  AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100')) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnStatusFailure(List<String> tid, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100')) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnStatusFailureAmount(List<String> tid);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100')) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnStatusFailure(String tid);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)   AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnStatusBothSuccessFailure(List<String> tid, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 )  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnStatusBothSuccessFailureAmount(List<String> tid);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnStatusBothSuccessFailure(String tid);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnType(List<String> tid, List<String> txnType, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnTypeAmount(List<String> tid, List<String> txnType);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnType(String tid, List<String> txnType);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3%  OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% )  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% )  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnTypeWithSearch(List<String> tid, List<String> txnType, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00'  AND bt.txn_type IN (?2)  AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% )  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnTypeWithSearchAmount(List<String> tid, List<String> txnType, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_type IN (?2) AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?3% OR mti LIKE ?3% AND txn_type LIKE ?3% OR txn_amount LIKE ?3% OR terminal_id LIKE ?3% OR merchant_id LIKE ?3% OR bt.txn_additional_amount LIKE ?3% OR bt.invoice_number LIKE ?3% OR bt.batch_number LIKE ?3% OR bt.rrn_number LIKE ?3% ) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnTypeWithSearch(String tid, List<String> txnType, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3)   AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataWithDateRange3(List<String> tid, String vfrom, String vto, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 )   ",
            nativeQuery = true
    )
    Double findTransactionDataWithDateRange3Amount(List<String> tid, String vfrom, String vto);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3)   AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataWithDateRange3(String tid, String vfrom, String vto);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% )   ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% )  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataWithDateRange3WithSearch(List<String> tid, String vfrom, String vto, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% )  ",
            nativeQuery = true
    )
    Double findTransactionDataWithDateRange3WithSearchAmount(List<String> tid, String vfrom, String vto, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND (bt.response_received_time >=?2 AND bt.response_received_time <=?3) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?4% OR mti LIKE ?4% AND txn_type LIKE ?4% OR txn_amount LIKE ?4% OR terminal_id LIKE ?4% OR merchant_id LIKE ?4% OR bt.txn_additional_amount LIKE ?4% OR bt.invoice_number LIKE ?4% OR bt.batch_number LIKE ?4% OR bt.rrn_number LIKE ?4% ) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataWithDateRange3WithSearch(String tid, String vfrom, String vto, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime, rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction  WHERE terminal_id in (?1)  AND txn_type NOT IN ('36') AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            countQuery = "SELECT count(*) FROM bijlipay_transaction  WHERE terminal_id in (?1)  AND txn_type NOT IN ('36') AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan))",
            nativeQuery = true
    )
    Page<Object[]> findAllTransactionData(List<String> tid, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction  WHERE terminal_id in (?1)  AND txn_type NOT IN ('36') AND txn_response_code ='00' AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 )",
            nativeQuery = true
    )
    Double findAllTransactionDataAmount(List<String> tid);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction  WHERE terminal_id=?1 AND txn_type NOT IN ('36') AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findAllTransactionData(String tid);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime, rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction  WHERE terminal_id in (?1)  AND txn_type NOT IN ('36') AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2% OR txn_additional_amount LIKE ?2% OR invoice_number LIKE ?2% OR batch_number LIKE ?2% OR rrn_number LIKE ?2% )  ",
            countQuery = "SELECT COUNT(*) FROM bijlipay_transaction  WHERE terminal_id in (?1)  AND txn_type NOT IN ('36') AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2%  OR txn_additional_amount LIKE ?2% OR invoice_number LIKE ?2% OR batch_number LIKE ?2% OR rrn_number LIKE ?2% )  ",
            nativeQuery = true
    )
    Page<Object[]> findAllTransactionDataWithSearch(List<String> tid, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction  WHERE terminal_id in (?1) AND txn_response_code ='00' AND txn_type NOT IN ('36') AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2%  OR txn_additional_amount LIKE ?2% OR invoice_number LIKE ?2% OR batch_number LIKE ?2% OR rrn_number LIKE ?2% )  ",
            nativeQuery = true
    )
    Double findAllTransactionDataWithSearchAmount(List<String> tid, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction  WHERE terminal_id=?1 AND txn_type NOT IN ('36') AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2%  OR txn_additional_amount LIKE ?2% OR invoice_number LIKE ?2% OR batch_number LIKE ?2% OR rrn_number LIKE ?2% ) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findAllTransactionDataWithSearch(String tid, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_response_code = '00' AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2% OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% )  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code = '00' AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2% OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% )  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnStatusSuccessSearch(List<String> tid, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_response_code = '00' AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2% OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% ) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnStatusSuccessSearch(String tid, String searchTerm);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code = '00' AND bt.txn_type NOT IN ('36','38','48') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2% OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% )  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnStatusSuccessSearchAmount(List<String> tid, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100')) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2% OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% )  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100')) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2%  OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% )  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnStatusFailureSearch(List<String> tid, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND (bt.txn_response_code NOT IN ('00') OR (bt.txn_type IN ('38', '48') AND bt.mti = '0100')) AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2% OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% ) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnStatusFailureSearch(String tid, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime,  rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount ,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode ,bin as bin ,customer_vpa as customerVpa  FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2% OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% )  ",
            countQuery = "SELECT count(*) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1)  AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2% OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% )  ",
            nativeQuery = true
    )
    Page<Object[]> findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch(List<String> tid, String searchTerm, Pageable pageable);

    @Query(
            value = "SELECT sum(txn_amount) FROM bijlipay_transaction bt WHERE bt.terminal_id in (?1) AND bt.txn_response_code ='00' AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND bt.rrn_number NOT IN (SELECT rrn_number FROM bijlipay_transaction  WHERE terminal_id IN (?1)  AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1 ) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2% OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% )  ",
            nativeQuery = true
    )
    Double findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearchAmount(List<String> tid, String searchTerm);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction bt WHERE bt.terminal_id =?1 AND bt.txn_type NOT IN ('36') AND bt.mti NOT IN ('0300', '0500') AND (bt.mti != '0100' OR bt.transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) AND (txn_response_code LIKE ?2% OR mti LIKE ?2% AND txn_type LIKE ?2% OR txn_amount LIKE ?2% OR terminal_id LIKE ?2% OR merchant_id LIKE ?2%  OR bt.txn_additional_amount LIKE ?2% OR bt.invoice_number LIKE ?2% OR bt.batch_number LIKE ?2% OR bt.rrn_number LIKE ?2% ) order by response_received_time DESC",
            nativeQuery = true
    )
    List<Object[]> findTransactionDataAllFilterWithTxnStatusBothSuccessFailureSearch(String tid, String searchTerm);

    @Query(
            value = "SELECT transaction_id AS id, mti AS mti, txn_type AS txnType, terminal_id AS terminalId, merchant_id AS merchantId, txn_date AS txnDate, txn_time AS txnTime, txn_amount AS txnAmount, txn_response_code AS txnResponseCode, response_received_time AS responseReceivedTime, rrn_number AS rRNumber, stan AS stan, invoice_number AS invoiceNumber, batch_number AS batchNumber, txn_additional_amount AS TxnAdditionalAmount,masked_card_number AS maskedCardNumber, panentry_mode as panEntryMode, bin as bin,customer_vpa as customerVpa  FROM bijlipay_transaction  WHERE terminal_id IN (?1) AND response_received_time >= CURDATE() - INTERVAL 30 DAY AND txn_type NOT IN ('36') AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) AND b2.response_received_time >= CURDATE() - INTERVAL 30 DAY GROUP BY b2.terminal_id, b2.stan)) order by response_received_time desc LIMIT 5",
            nativeQuery = true
    )
    List<Tuple> findLastFiveSwitchTransactionBasedOnUser(List<String> tid);

    @Query(
            value = "SELECT response_received_time AS TxnDate, merchant_id AS Mid, terminal_id AS Tid, txn_type AS TxnType, rrn_number AS RRN, txn_amount AS TxnAmount, txn_additional_amount AS TxnAdditionalAmount, mti AS mti  FROM bijlipay_transaction  WHERE terminal_id=?3 AND (response_received_time >=?1 AND response_received_time <=?2) AND txn_type NOT IN ('36') AND mti NOT IN ('0300', '0500') AND (mti != '0100' OR transaction_id IN (SELECT MAX(b2.transaction_id) FROM bijlipay_transaction b2 WHERE b2.mti = '0100' AND b2.terminal_id IN (?1) GROUP BY b2.terminal_id, b2.stan)) order by response_received_time DESC",
            nativeQuery = true
    )
    @Transactional(
            readOnly = true
    )
    List<Object[]> fetchTidBasedTxnData(String vfrom, String vto, String tid);

    @Query(
            value = "SELECT distinct stan FROM bijlipay_transaction WHERE terminal_id =?1 AND  mti ='0200' and txn_response_code ='00' and txn_type ='00'  AND DATE(response_received_time) BETWEEN DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND CURDATE()",
            nativeQuery = true
    )
    List<String> findTodayAndYesterdayStan(String tid);
}