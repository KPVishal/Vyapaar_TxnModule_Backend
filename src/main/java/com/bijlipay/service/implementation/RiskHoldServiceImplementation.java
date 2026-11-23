package com.bijlipay.service.implementation;

import com.bijlipay.controller.BaseController;
import com.bijlipay.model.Category;
import com.bijlipay.model.RiskHold;
import com.bijlipay.model.User;
import com.bijlipay.model.UserTidVpaData;
import com.bijlipay.model.dto.RiskHoldDto;
import com.bijlipay.repository.CategoryRepository;
import com.bijlipay.repository.RiskHoldRepository;
import com.bijlipay.repository.UserRepository;
import com.bijlipay.service.RiskHoldService;
import com.bijlipay.service.UserTidVpaDataService;
import com.bijlipay.util.Constant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RiskHoldServiceImplementation extends BaseController implements RiskHoldService {
    private final Logger logger = LoggerFactory.getLogger(RiskHoldServiceImplementation.class);
    @Autowired
    RiskHoldRepository riskHoldRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserTidVpaDataService userTidVpaDataService;
    @Autowired
    private CategoryRepository categoryRepository;

    public int save(List<RiskHoldDto> riskHoldDto) {
        if (riskHoldDto.size() > 0) {
            riskHoldDto.forEach((l) -> {
                List<UserTidVpaData> mids = this.userTidVpaDataService.findByMid(l.getMid());
                if (mids.size() > 0) {
                    this.logger.info("Risk Hold Data mapping values Started: ---- :");
                    RiskHold result = new RiskHold();
                    result.setMid(l.getMid());
                    result.setTid(l.getTid());
                    result.setCardNumber(l.getCardNumber());
                    result.setCardType(l.getCardType());
                    result.setTxnType(l.getTxnType());
                    result.setRrn(l.getRrn());
                    this.logger.info("The TXN Date : ---- : {}", l.getTxnDate());
                    result.setTxnDate(l.getTxnDate());
                    result.setTxnAmount(l.getTxnAmount());
                    this.logger.info("The Hold Date : ---- : {}", l.getHoldDate());
                    result.setHoldDate(l.getHoldDate());
                    result.setHoldRemark(l.getHoldRemark());
                    result.setNetwork(l.getNetwork());
                    result.setRiskHoldId(l.getRiskHoldId());
                    this.riskHoldRepository.save(result);
                    this.logger.info("Risk Hold Data mapping values DONE: ---- : {} ", result);
                    this.logger.info("Notification Data Creation Started : ---- :");
                    UserTidVpaData userTidVpaData = this.userTidVpaDataService.findByTid(result.getTid());
                    if (userTidVpaData != null) {
                        User user = userTidVpaData.getUser();
                        if (user != null) {
                            Category category = new Category();
                            category.setCategoryType(1);
                            category.setDescription("Risk Hold Created on TID : " + result.getTid());
                            category.setUserTidVpaData(userTidVpaData);
                            category.setUser(user);
                            Category finalResponse = (Category)this.categoryRepository.save(category);
                            String redirectTo = Constant.commonType(category.getCategoryType());
                            Logger var10000 = this.logger;
                            String var10002 = result.getTid();
                            var10000.info("Notification data push : title       : ---- : {}", var10002 + "_" + redirectTo);
                            this.logger.info("Notification data push : User        : ---- : {}", user.getEmail());
                            this.logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                            this.logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                            this.logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                            this.sendPushNotificationGenerate(redirectTo + " on TID: " + result.getTid(), finalResponse.getDescription(), finalResponse, redirectTo, user);
                            this.logger.info("Notification Data Creation Completed : ---- :");
                        }
                    }
                } else {
                    this.logger.info("The Merchant account is not registered in Merchant application : ------- : {}", l.getMid());
                }

            });
            return 0;
        } else {
            return 1;
        }
    }

    public int update(List<RiskHoldDto> riskHoldDto) {
        if (riskHoldDto.size() > 0) {
            riskHoldDto.forEach((l) -> {
                List<UserTidVpaData> mids = this.userTidVpaDataService.findByMid(l.getMid());
                if (mids.size() > 0) {
                    this.logger.info("Risk Hold Data Status update is started : ---- : {}", l);
                    RiskHold result = this.riskHoldRepository.findByRiskHoldId(l.getRiskHoldId());
                    if (result != null) {
                        result.setStatus(l.getStatus());
                        result.setHoldRemark(l.getHoldRemark());
                        this.riskHoldRepository.save(result);
                        this.logger.info("Risk Hold Data mapping values DONE: ---- : {} ", result);
                        this.logger.info("Notification Data Creation Started : ---- :");
                        UserTidVpaData userTidVpaData = this.userTidVpaDataService.findByTid(result.getTid());
                        User user = userTidVpaData.getUser();
                        Category category = new Category();
                        category.setCategoryType(1);
                        category.setDescription("Risk Hold Status Updated on TID : " + result.getTid());
                        category.setUserTidVpaData(userTidVpaData);
                        category.setUser(user);
                        Category finalResponse = (Category)this.categoryRepository.save(category);
                        String redirectTo = Constant.commonType(category.getCategoryType());
                        Logger var10000 = this.logger;
                        String var10002 = result.getTid();
                        var10000.info("Notification data push : title       : ---- : {}", var10002 + "_" + redirectTo);
                        this.logger.info("Notification data push : User        : ---- : {}", user.getEmail());
                        this.logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                        this.logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                        this.logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                        this.sendPushNotificationGenerate(redirectTo + " on TID: " + result.getTid(), finalResponse.getDescription(), finalResponse, redirectTo, user);
                        this.logger.info("Notification Data Creation Completed : ---- :");
                    }
                } else {
                    this.logger.info("The Merchant account is not registered in Merchant application : ------- : {}", l.getMid());
                }

            });
            return 0;
        } else {
            return 1;
        }
    }

    public Page<RiskHold> getRiskHolds(Pageable pageable, long sessionUserId, Optional<Integer> action, Optional<String> tid) {
        this.logger.info("Getting Risk Hold Data of merchant with TID {}", tid);
        List<String> tidList = this.userTidVpaDataService.fetchTidData(this.userRepository.findByUser(this.getSessionUser().getId()));
        if (action.isPresent()) {
            if ((Integer)action.get() == 1) {
                Page<RiskHold> resultPage;
                if (tid.isPresent()) {
                    resultPage = this.riskHoldRepository.findListOphPagedCompletedWithTid((String)tid.get(), "Release", pageable);
                } else {
                    resultPage = this.riskHoldRepository.findListOphPagedCompleted(tidList, "Release", pageable);
                }

                this.logger.info("Risk hold data : ---- : {}", resultPage.getContent());
                return resultPage;
            } else if ((Integer)action.get() == 2) {
                Page<RiskHold> resultPage;
                if (tid.isPresent()) {
                    resultPage = this.riskHoldRepository.findListOphPagedInProgressWithTid((String)tid.get(), pageable);
                } else {
                    resultPage = this.riskHoldRepository.findListOphPagedInProgress(tidList, pageable);
                }

                this.logger.info("Risk hold data : ---- : {}", resultPage.getContent());
                return resultPage;
            } else {
                return null;
            }
        } else {
            Page<RiskHold> resultPage = this.riskHoldRepository.findListOphPaged(tidList, pageable);
            this.logger.info("Risk hold data : ---- : {}", resultPage.getContent());
            return resultPage;
        }
    }
}