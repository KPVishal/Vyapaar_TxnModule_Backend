package com.bijlipay.service.implementation;

import com.bijlipay.exception.UserNotFoundException;
import com.bijlipay.model.User;
import com.bijlipay.model.UserTidVpaData;
import com.bijlipay.repository.UserRepository;
import com.bijlipay.repository.UserTidVpaDataRepository;
import com.bijlipay.service.UserTidVpaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserTidVpaDataServiceImpl implements UserTidVpaDataService {

    private static final Logger logger = LoggerFactory.getLogger(UserTidVpaDataServiceImpl.class);

    @Autowired
    UserTidVpaDataRepository userTidVpaDataRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public UserTidVpaData checkVpa(String vpa) {
        return userTidVpaDataRepository.findByVpa(vpa);
    }

    @Override
    @Transactional("primaryTransactionManager")
    public UserTidVpaData findByTid(String tid) {
        return userTidVpaDataRepository.findByTid(tid);
    }

    @Override
    @Transactional("primaryTransactionManager")
    public List<String> findByVpa(User user) {
        return userTidVpaDataRepository.findByVpaBasedOnUser(user);
    }

    @Override
    @Transactional("primaryTransactionManager")
    public List<String> fetchTidData(User user) {
        return userTidVpaDataRepository.fetchTid(user, false);
    }

    @Override
    @Transactional("primaryTransactionManager")
    public List<UserTidVpaData> getByUserId(User sessionUserId) {
        return userTidVpaDataRepository.findByUser(sessionUserId);
    }

    @Override
    public List<UserTidVpaData> findByMid(String mid) {
        return userTidVpaDataRepository.findByMid(mid);
    }

    @Override
    public User getUserById(long userId) {
        logger.info("getUserId user : ");
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Id Not Found"));
    }
}
