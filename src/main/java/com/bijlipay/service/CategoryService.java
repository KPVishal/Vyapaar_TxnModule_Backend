package com.bijlipay.service;

import com.bijlipay.model.Category;
import com.bijlipay.model.SettlementRequestResponse;
import com.bijlipay.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    List<Category> findByTid(Optional<String> tid, Optional<Integer> action, Optional<Long> categoryId, long sessionUserId);

    List<Category> findByUser(long sessionUserId);

    Page<SettlementRequestResponse> getSettlementReportData(User userById, Pageable pageable);

    Page<SettlementRequestResponse> getSettlementReportDataBasedOnCategory(User userById, String s, Pageable pageable);
}