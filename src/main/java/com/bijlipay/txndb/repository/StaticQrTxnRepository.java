package com.bijlipay.txndb.repository;

import com.bijlipay.txndb.model.StaticQrTxn;
import java.util.List;
import javax.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StaticQrTxnRepository extends JpaRepository<StaticQrTxn, Long> {
    @Query(
            value = "select  customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount,\ngateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode,created_at from static_qr_txn  where credit_vpa in(?1) and gateway_response_code ='00' order by transaction_timestamp desc LIMIT 5",
            nativeQuery = true
    )
    List<Tuple> findLastFiveAxisStaticQrTxn(String vpaDetails);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount,gateway_transaction_id,\ngateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode,created_at from static_qr_txn where credit_vpa in (?3) and gateway_response_code ='00' and transaction_timestamp >= ?1 and transaction_timestamp <= ?2 and (credit_vpa LIKE ?4% OR rrn LIKE ?4% OR transaction_amount LIKE ?4%)",
            countQuery = "select COUNT(*) from static_qr_txn where credit_vpa in (?3) and gateway_response_code ='00' and transaction_timestamp >= ?1 and transaction_timestamp <= ?2 and (credit_vpa LIKE ?4% OR rrn LIKE ?4%  OR transaction_amount LIKE ?4%)",
            nativeQuery = true
    )
    Page<Object[]> findAxisQrTxnBasedOnVpaWithDatesAndSearchTerm(String from, String to, List<String> vpaDetails, String searchTerm, Pageable pageable);

    @Query(
            value = "select sum(transaction_amount) from static_qr_txn where credit_vpa in (?3) and gateway_response_code ='00' and created_at >= ?1 and created_at <= ?2 and (credit_vpa LIKE ?4% OR rrn LIKE ?4% OR transaction_amount LIKE ?4%) order by created_at DESC",
            nativeQuery = true
    )
    Double findAxisQrTxnBasedOnVpaWithDatesAndSearchTermAmount(String from, String to, List<String> vpaDetails, String searchTerm);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount, gateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode from static_qr_txn where credit_vpa in (?3) and gateway_response_code ='00' and created_at >= ?1 and created_at <= ?2 and (credit_vpa LIKE ?4% OR rrn LIKE ?4% OR transaction_amount LIKE ?4%) order by created_at DESC",
            nativeQuery = true
    )
    List<Object[]> findAxisQrTxnBasedOnVpaWithDatesAndSearchTerm(String from, String to, String vpaDetails, String searchTerm);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount,\ngateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode,created_at from static_qr_txn where credit_vpa in (?3) and gateway_response_code ='00' and transaction_timestamp >= ?1 and transaction_timestamp <= ?2",
            countQuery = "select COUNT(*) from static_qr_txn where credit_vpa in (?3) and gateway_response_code ='00' and transaction_timestamp >= ?1 and transaction_timestamp <= ?2",
            nativeQuery = true
    )
    Page<Object[]> findAxisQrBasedOnVpaWithDates(String vfrom, String vto, List<String> vpaDetails, Pageable pageable);

    @Query(
            value = "select sum(transaction_amount) from static_qr_txn where credit_vpa in (?3) and gateway_response_code ='00' and created_at >= ?1 and created_at <= ?2",
            nativeQuery = true
    )
    Double findAxisQrBasedOnVpaWithDatesAmount(String vfrom, String vto, List<String> vpaDetails);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount, gateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode from static_qr_txn where credit_vpa in(?3) and gateway_response_code ='00' and created_at >= ?1 and created_at <= ?2 order by created_at DESC",
            nativeQuery = true
    )
    List<Object[]> findAxisQrBasedOnVpaWithDates(String vfrom, String vto, List<String> vpaDetails);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount, gateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode from static_qr_txn where credit_vpa in(?3) and gateway_response_code ='00' and created_at >= ?1 and created_at <= ?2 order by created_at DESC",
            nativeQuery = true
    )
    List<Object[]> findAxisQrBasedOnVpaWithDates(String vfrom, String vto, String vpaDetails);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount,\ngateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode,created_at from static_qr_txn \nwhere credit_vpa in (?1) and gateway_response_code ='00' and (credit_vpa LIKE ?2% OR rrn LIKE ?2% OR transaction_amount LIKE ?2%)",
            countQuery = "select COUNT(*) from static_qr_txn \nwhere credit_vpa in (?1) and gateway_response_code ='00' and (credit_vpa LIKE ?2% OR rrn LIKE ?2% OR transaction_amount LIKE ?2%)",
            nativeQuery = true
    )
    Page<Object[]> findAxisQrTxnBasedOnVpaWithSearchTerm(List<String> vpaDetails, String searchTerm, Pageable pageable);

    @Query(
            value = "select sum(transaction_amount) from static_qr_txn where credit_vpa in (?1) and gateway_response_code ='00' and (credit_vpa LIKE ?2% OR rrn LIKE ?2% OR transaction_amount LIKE ?2%)",
            nativeQuery = true
    )
    Double findAxisQrTxnBasedOnVpaWithSearchTermAmount(List<String> vpaDetails, String searchTerm);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount, gateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode from static_qr_txn where credit_vpa in (?1) and gateway_response_code ='00' and (credit_vpa LIKE ?2% OR rrn LIKE ?2% OR transaction_amount LIKE ?2%) order by created_at DESC",
            nativeQuery = true
    )
    List<Object[]> findAxisQrTxnBasedOnVpaWithSearchTerm(List<String> vpaDetails, String searchTerm);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount, gateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode from static_qr_txn where credit_vpa in (?1) and gateway_response_code ='00' and (credit_vpa LIKE ?2% OR rrn LIKE ?2% OR transaction_amount LIKE ?2%) order by created_at DESC",
            nativeQuery = true
    )
    List<Object[]> findAxisQrTxnBasedOnVpaWithSearchTerm(String vpaDetails, String searchTerm);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount,\ngateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode,created_at from static_qr_txn \nwhere credit_vpa in (?1)  and gateway_response_code ='00'",
            countQuery = "select COUNT(*) from static_qr_txn where credit_vpa in (?1) and gateway_response_code ='00'",
            nativeQuery = true
    )
    Page<Object[]> findAxisQrTxn(List<String> vpaDetails, Pageable pageable);

    @Query(
            value = "select sum(transaction_amount) from static_qr_txn where credit_vpa in (?1) and gateway_response_code ='00'",
            nativeQuery = true
    )
    Double findAxisQrTxnAmount(List<String> vpaDetails);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount, gateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode from static_qr_txn \nwhere credit_vpa in (?1) and gateway_response_code ='00' order by created_at DESC",
            nativeQuery = true
    )
    List<Object[]> findAxisQrTxn(List<String> vpaDetails);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount, gateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode from static_qr_txn \nwhere credit_vpa in (?1) and gateway_response_code ='00' order by created_at DESC",
            nativeQuery = true
    )
    List<Object[]> findAxisQrTxn(String vpaDetails);

    @Query(
            value = "select * from static_qr_txn where rrn = ?1",
            nativeQuery = true
    )
    StaticQrTxn findByAxisQrTxnBasedOnRRN(String rrn);

    @Query(
            value = "SELECT SUM(CASE WHEN DATE(transaction_timestamp) = CURDATE() THEN transaction_amount ELSE 0 END) AS today_sum,COUNT(CASE WHEN DATE(transaction_timestamp) = CURDATE() THEN 1 ELSE NULL END) AS today_count,SUM(CASE WHEN DATE(transaction_timestamp) = CURDATE() - INTERVAL 1 DAY THEN transaction_amount ELSE 0 END) AS yesterday_sum,COUNT(CASE WHEN DATE(transaction_timestamp) = CURDATE() - INTERVAL 1 DAY THEN 1 ELSE NULL END) AS yesterday_count  FROM static_qr_txn WHERE credit_vpa=?1 AND gateway_response_code='00' AND transaction_timestamp >= CURDATE() - INTERVAL 1 DAY",
            nativeQuery = true
    )
    List<Object[]> fetchSumAndCountOfTodayAndYesterdayTxn(String vpa);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount,\ngateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode,created_at from static_qr_txn where credit_vpa = ?1 and gateway_response_code ='00' and transaction_timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY)",
            countQuery = "select count(*) from static_qr_txn where credit_vpa = ?1 and gateway_response_code ='00' and transaction_timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY)",
            nativeQuery = true
    )
    Page<Object[]> findAxisQrTxnLast30DaysData(List<String> vpaDetails, Pageable pageable);

    @Query(
            value = "select sum(transaction_amount) from static_qr_txn where credit_vpa in (?1) and gateway_response_code ='00' and created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)",
            nativeQuery = true
    )
    Double findAxisQrTxnLast30DaysDataAmount(List<String> vpaDetails);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount, gateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode from static_qr_txn where credit_vpa in(?1) and gateway_response_code ='00' and created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) order by created_at DESC",
            nativeQuery = true
    )
    List<Object[]> findAxisQrTxnLast30DaysData(List<String> vpaDetails);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount,\ngateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode,created_at from static_qr_txn where credit_vpa = ?1 and gateway_response_code ='00' and YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE())",
            countQuery = "select COUNT(*) from static_qr_txn where credit_vpa = ?1 and gateway_response_code ='00' and YEAR(transaction_timestamp) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE())",
            nativeQuery = true
    )
    Page<Object[]> findAxisQrTxnThisMonthData(String vpaDetails, Pageable pageable);

    @Query(
            value = "select sum(transaction_amount) from static_qr_txn where credit_vpa in (?1)and gateway_response_code ='00' and YEAR(transaction_timestamp) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE())",
            nativeQuery = true
    )
    Double findAxisQrTxnThisMonthDataSum(List<String> vpaDetails);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount, gateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode from static_qr_txn where credit_vpa in(?1) and gateway_response_code ='00' and YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE()) order by created_at DESC",
            nativeQuery = true
    )
    List<Object[]> findAxisQrTxnThisMonthData(List<String> vpaDetails);

    @Query(
            value = "select  customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount,\ngateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode,created_at from static_qr_txn  where credit_vpa in(?1) and gateway_response_code ='00' order by created_at desc LIMIT 5",
            nativeQuery = true
    )
    List<Tuple> findLastFiveAxisStaticQrTxnBasedOnUser(List<String> vpaDetails);

    @Query(
            value = "select customer_vpa,credit_vpa,merchant_id,merchant_channel_id,merchant_transaction_id,transaction_timestamp,round(transaction_amount,2) as transaction_amount, gateway_transaction_id,gateway_response_code,gateway_response_message,rrn,checksum,payeractype,purpose_code,init_mode from static_qr_txn \nwhere credit_vpa = ?3 and gateway_response_code ='00' AND (created_at >= ?1 and created_at <= ?2) order by created_at DESC",
            nativeQuery = true
    )
    @Transactional(
            readOnly = true
    )
    List<Object[]> fetchTidBasedTxnData(String vfrom, String vto, String vpa);
}