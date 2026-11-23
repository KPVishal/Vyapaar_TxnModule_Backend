package com.bijlipay.service.implementation;

import com.bijlipay.exception.UserNotFoundException;
import com.bijlipay.model.User;
import com.bijlipay.model.UserTidVpaData;
import com.bijlipay.repository.UserRepository;
import com.bijlipay.repository.UserTidVpaDataRepository;
import com.bijlipay.service.UserTidVpaDataService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserTidVpaDataServiceImpl implements UserTidVpaDataService {
    private static final Logger logger = LoggerFactory.getLogger(UserTidVpaDataServiceImpl.class);
    @Autowired
    UserTidVpaDataRepository userTidVpaDataRepository;
    @Autowired
    UserRepository userRepository;

    public UserTidVpaData checkVpa(String vpa) {
        return this.userTidVpaDataRepository.findByVpa(vpa);
    }

    @Transactional("primaryTransactionManager")
    public UserTidVpaData findByTid(String tid) {
        return this.userTidVpaDataRepository.findByTid(tid);
    }

    @Transactional("primaryTransactionManager")
    public List<String> findByVpa(User user) {
        return this.userTidVpaDataRepository.findByVpaBasedOnUser(user);
    }

    @Transactional("primaryTransactionManager")
    public List<String> fetchTidData(User user) {
        return this.userTidVpaDataRepository.fetchTid(user, false);
    }

    @Transactional("primaryTransactionManager")
    public List<UserTidVpaData> getByUserId(User sessionUserId) {
        return this.userTidVpaDataRepository.findByUser(sessionUserId);
    }

    public List<UserTidVpaData> findByMid(String mid) {
        return this.userTidVpaDataRepository.findByMid(mid);
    }

    public User getUserById(long userId) {
        logger.info("getUserId user : ");
        return (User)this.userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Id Not Found"));
    }
}