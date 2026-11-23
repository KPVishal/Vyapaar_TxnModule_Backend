package com.bijlipay.repository;

import com.bijlipay.model.RiskHold;
import java.util.List;
import javax.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface RiskHoldRepository extends JpaRepository<RiskHold, Long> {
    @Query("SELECT r FROM RiskHold r WHERE r.tid IN (?1) ")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<RiskHold> findListOphPaged(List<String> tid, Pageable pageable);

    @Query("SELECT r FROM RiskHold r WHERE r.tid = ?1 AND r.status LIKE %?2%")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<RiskHold> findListOphPagedCompletedWithTid(String tid, String status, Pageable pageable);

    @Query("SELECT r FROM RiskHold r WHERE r.tid IN (?1) AND r.status LIKE %?2%")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<RiskHold> findListOphPagedCompleted(List<String> tidList, String status, Pageable pageable);

    @Query(
            value = "SELECT SUM(CAST(r.txn_amount AS FLOAT)) AS total_amt FROM risk_hold r WHERE r.tid= ?1 AND (r.status NOT IN ('Release') OR r.status IS NULL)",
            nativeQuery = true
    )
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    String sumOfTid(String tid);

    @Query("SELECT r FROM RiskHold r WHERE r.riskHoldId = ?1 ORDER BY r.id DESC")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    RiskHold findByRiskHoldId(String riskHoldId);

    @Query("SELECT r FROM RiskHold r WHERE r.tid = ?1 AND r.status IS NULL")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<RiskHold> findListOphPagedInProgressWithTid(String s, Pageable pageable);

    @Query("SELECT r FROM RiskHold r WHERE r.tid IN (?1) AND r.status IS NULL")
    @QueryHints({@QueryHint(
            name = "org.hibernate.cacheable",
            value = "true"
    )})
    Page<RiskHold> findListOphPagedInProgress(List<String> tidList, Pageable pageable);
}