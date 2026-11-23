package com.bijlipay.service;

import com.bijlipay.model.User;
import com.bijlipay.model.UserTidVpaData;
import java.util.List;

public interface UserTidVpaDataService {
    UserTidVpaData findByTid(String tid);

    UserTidVpaData checkVpa(String vpa);

    List<String> findByVpa(User user);

    List<String> fetchTidData(User user);

    List<UserTidVpaData> getByUserId(User user);

    List<UserTidVpaData> findByMid(String mid);

    User getUserById(long id);
}