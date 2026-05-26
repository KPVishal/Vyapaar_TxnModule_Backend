package com.bijlipay.service;

import com.bijlipay.model.User;
import com.bijlipay.model.UserAuthentication;

public interface UserAuthenticationService {
    UserAuthentication getUser(long l);

    void insertdata(String jwtToken, User userData);

    void update(String jwtToken, Long long1);
}
