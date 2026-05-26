package com.bijlipay.service.implementation;

import com.bijlipay.controller.BaseController;
import com.bijlipay.model.Category;
import com.bijlipay.model.UserTidVpaData;
import com.bijlipay.model.RiskHold;
import com.bijlipay.model.User;
import com.bijlipay.model.dto.RiskHoldDto;
import com.bijlipay.repository.CategoryRepository;
import com.bijlipay.repository.RiskHoldRepository;
import com.bijlipay.repository.UserRepository;
import com.bijlipay.security.UserPrincipal;
import com.bijlipay.service.UserTidVpaDataService;
import com.bijlipay.service.RiskHoldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.bijlipay.util.Constant.*;

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

    @Override
    public int save(List<RiskHoldDto> riskHoldDto) {
        if (riskHoldDto.size() > 0) {
            riskHoldDto.forEach(l -> {
                List<UserTidVpaData> mids = userTidVpaDataService.findByMid(l.getMid());
                if (mids.size() > 0) {
                    logger.info("Risk Hold Data mapping values Started: ---- :");
                    RiskHold result = new RiskHold();
                    result.setMid(l.getMid());
                    result.setTid(l.getTid());
                    result.setCardNumber(l.getCardNumber());
                    result.setCardType(l.getCardType());
                    result.setTxnType(l.getTxnType());
                    result.setRrn(l.getRrn());
                    logger.info("The TXN Date : ---- : {}", l.getTxnDate());
                    result.setTxnDate(l.getTxnDate());
                    result.setTxnAmount(l.getTxnAmount());
                    logger.info("The Hold Date : ---- : {}", l.getHoldDate());
                    result.setHoldDate(l.getHoldDate());
                    result.setHoldRemark(l.getHoldRemark());
                    result.setNetwork(l.getNetwork());
                    result.setRiskHoldId(l.getRiskHoldId());
                    riskHoldRepository.save(result);
                    logger.info("Risk Hold Data mapping values DONE: ---- : {} ", result);
                    logger.info("Notification Data Creation Started : ---- :");
                    UserTidVpaData userTidVpaData = userTidVpaDataService.findByTid(result.getTid());
                    if (userTidVpaData != null) {
                        User user = userTidVpaData.getUser();
                        if (user != null) {
                            Category category = new Category();
                            category.setCategoryType(RISH_HOLD_ID);
                            category.setDescription("Risk Hold Created on TID : " + result.getTid());
                            category.setUserTidVpaData(userTidVpaData);
                            category.setUser(user);
                            Category finalResponse = categoryRepository.save(category);
                            String redirectTo = commonType(category.getCategoryType());
                            logger.info("Notification data push : title       : ---- : {}", result.getTid() + "_" + redirectTo);
                            logger.info("Notification data push : User        : ---- : {}", user.getEmail());
                            logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                            logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                            logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                            sendPushNotificationGenerate(redirectTo + " on TID: " + result.getTid(), finalResponse.getDescription(), finalResponse, redirectTo, user);
                            logger.info("Notification Data Creation Completed : ---- :");
                        }
                    }
                } else {
                    logger.info("The Merchant account is not registered in Merchant application : ------- : {}", l.getMid());
                }
            });
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int update(List<RiskHoldDto> riskHoldDto) {
        if (riskHoldDto.size() > 0) {
            riskHoldDto.forEach(l -> {
                List<UserTidVpaData> mids = userTidVpaDataService.findByMid(l.getMid());
                if (mids.size() > 0) {
                    logger.info("Risk Hold Data Status update is started : ---- : {}", l);
                    RiskHold result = riskHoldRepository.findByRiskHoldId(l.getRiskHoldId());
                    if (result != null) {
                        result.setStatus(l.getStatus());
                        result.setHoldRemark(l.getHoldRemark());
                        riskHoldRepository.save(result);
                        logger.info("Risk Hold Data mapping values DONE: ---- : {} ", result);
                        logger.info("Notification Data Creation Started : ---- :");
                        UserTidVpaData userTidVpaData = userTidVpaDataService.findByTid(result.getTid());
                        User user = userTidVpaData.getUser();
                        Category category = new Category();
                        category.setCategoryType(RISH_HOLD_ID);
                        category.setDescription("Risk Hold Status Updated on TID : " + result.getTid());
                        category.setUserTidVpaData(userTidVpaData);
                        category.setUser(user);
                        Category finalResponse = categoryRepository.save(category);
                        String redirectTo = commonType(category.getCategoryType());
                        logger.info("Notification data push : title       : ---- : {}", result.getTid() + "_" + redirectTo);
                        logger.info("Notification data push : User        : ---- : {}", user.getEmail());
                        logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                        logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                        logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                        sendPushNotificationGenerate(redirectTo + " on TID: " + result.getTid(), finalResponse.getDescription(), finalResponse, redirectTo, user);
                        logger.info("Notification Data Creation Completed : ---- :");
                    }
                } else {
                    logger.info("The Merchant account is not registered in Merchant application : ------- : {}", l.getMid());
                }
            });
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public Page<RiskHold> getRiskHolds(Pageable pageable, long sessionUserId, Optional<Integer> action, Optional<String> tid) {
        logger.info("Getting Risk Hold Data of merchant with TID {}", tid);
        List<String> tidList = userTidVpaDataService.fetchTidData(userRepository.findByUser(getSessionUser().getId()));
        Page<RiskHold> resultPage;
        if (action.isPresent()) {
            if (action.get() == RISK_HOLD_STATUS_COMPLETED) {
                if (tid.isPresent()) {
                    resultPage = riskHoldRepository.findListOphPagedCompletedWithTid(tid.get(),RISK_HOLD_COMPLETED, pageable);
                } else {
                    resultPage = riskHoldRepository.findListOphPagedCompleted(tidList,RISK_HOLD_COMPLETED, pageable);
                }
                logger.info("Risk hold data : ---- : {}", resultPage.getContent());
                return resultPage;
            } else if (action.get() == RISK_HOLD_STATUS_PENDING) {
                if (tid.isPresent()) {
                    resultPage = riskHoldRepository.findListOphPagedInProgressWithTid(tid.get(), pageable);
                } else {
                    resultPage = riskHoldRepository.findListOphPagedInProgress(tidList, pageable);
                }
                logger.info("Risk hold data : ---- : {}", resultPage.getContent());
                return resultPage;
            }else {
                return null;
            }
        } else {
            resultPage = riskHoldRepository.findListOphPaged(tidList, pageable);
            logger.info("Risk hold data : ---- : {}", resultPage.getContent());
            return resultPage;
        }
    }
}
