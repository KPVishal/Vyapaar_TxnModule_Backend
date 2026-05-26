package com.bijlipay.service.implementation;

import com.bijlipay.model.Category;
import com.bijlipay.model.SettlementRequestResponse;
import com.bijlipay.model.User;
import com.bijlipay.repository.CategoryRepository;
import com.bijlipay.repository.SettlementRequestResponseRepository;
import com.bijlipay.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImplmplementation implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SettlementRequestResponseRepository settlementRequestResponseRepository;

    @Override
    public List<Category> findByTid(Optional<String> tid, Optional<Integer> action, Optional<Long> categoryId, long sessionUserId) {
        if (tid.isPresent() && action.isPresent() && categoryId.isPresent()) {
            return categoryRepository.getRequestById(tid.get(), action.get(), categoryId.get());
        } else if (tid.isPresent() && action.isPresent()) {
            return categoryRepository.getRequestByAction(tid.get(), action.get());
        } else {
            return categoryRepository.getByUser(sessionUserId);
        }
    }

    @Override
    public List<Category> findByUser(long sessionUserId) {
        return categoryRepository.getByUser(sessionUserId);
    }

    @Override
    public Page<SettlementRequestResponse> getSettlementReportData(User userById, Pageable pageable) {
        return settlementRequestResponseRepository.findSettlementDateBasedOnUser(userById,pageable);
    }

    @Override
    public Page<SettlementRequestResponse> getSettlementReportDataBasedOnCategory(User userById, String categoryType, Pageable pageable) {
        return settlementRequestResponseRepository.findSettlementDateBasedOnUserAndCategory(userById,categoryType,pageable);
    }
}
