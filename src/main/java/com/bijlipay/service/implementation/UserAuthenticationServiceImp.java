package com.bijlipay.service.implementation;

import com.bijlipay.model.User;
import com.bijlipay.model.UserAuthentication;
import com.bijlipay.repository.UserAuthenticationRepo;
import com.bijlipay.service.UserAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAuthenticationServiceImp implements UserAuthenticationService {
    @Autowired
    private UserAuthenticationRepo userAuthenticateRepo;

    public UserAuthentication getUser(long l) {
        UserAuthentication username = this.userAuthenticateRepo.getuser(l);
        return username;
    }

    public void insertdata(String jwtToken, User userData) {
        UserAuthentication userAuthentication = new UserAuthentication();
        userAuthentication.setToken(jwtToken);
        userAuthentication.setUser(userData);
        this.userAuthenticateRepo.save(userAuthentication);
    }

    public void update(String jwtToken, Long long1) {
        this.userAuthenticateRepo.update(jwtToken, long1);
    }
}