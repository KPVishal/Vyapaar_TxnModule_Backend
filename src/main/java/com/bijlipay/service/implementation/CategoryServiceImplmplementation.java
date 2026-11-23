package com.bijlipay.service.implementation;

import com.bijlipay.model.Category;
import com.bijlipay.model.SettlementRequestResponse;
import com.bijlipay.model.User;
import com.bijlipay.repository.CategoryRepository;
import com.bijlipay.repository.SettlementRequestResponseRepository;
import com.bijlipay.service.CategoryService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImplmplementation implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private SettlementRequestResponseRepository settlementRequestResponseRepository;

    public List<Category> findByTid(Optional<String> tid, Optional<Integer> action, Optional<Long> categoryId, long sessionUserId) {
        if (tid.isPresent() && action.isPresent() && categoryId.isPresent()) {
            return this.categoryRepository.getRequestById((String)tid.get(), (Integer)action.get(), (Long)categoryId.get());
        } else {
            return tid.isPresent() && action.isPresent() ? this.categoryRepository.getRequestByAction((String)tid.get(), (Integer)action.get()) : this.categoryRepository.getByUser(sessionUserId);
        }
    }

    public List<Category> findByUser(long sessionUserId) {
        return this.categoryRepository.getByUser(sessionUserId);
    }

    public Page<SettlementRequestResponse> getSettlementReportData(User userById, Pageable pageable) {
        return this.settlementRequestResponseRepository.findSettlementDateBasedOnUser(userById, pageable);
    }

    public Page<SettlementRequestResponse> getSettlementReportDataBasedOnCategory(User userById, String categoryType, Pageable pageable) {
        return this.settlementRequestResponseRepository.findSettlementDateBasedOnUserAndCategory(userById, categoryType, pageable);
    }
}