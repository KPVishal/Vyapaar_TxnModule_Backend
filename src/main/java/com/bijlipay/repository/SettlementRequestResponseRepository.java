package com.bijlipay.repository;

import com.bijlipay.model.SettlementRequestResponse;
import com.bijlipay.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRequestResponseRepository extends JpaRepository<SettlementRequestResponse,Long> {
    @Query("SELECT s FROM SettlementRequestResponse s WHERE s.uniqueId =?1")
    SettlementRequestResponse findByUniqueId(String uniqueId);

    @Query("SELECT s FROM SettlementRequestResponse s WHERE s.user = ?1")
    Page<SettlementRequestResponse> findSettlementDateBasedOnUser(User userById, Pageable pageable);

    @Query("SELECT s FROM SettlementRequestResponse s WHERE s.user = ?1 AND s.categoryType=?2")
    Page<SettlementRequestResponse> findSettlementDateBasedOnUserAndCategory(User userById, String categoryType, Pageable pageable);
}
