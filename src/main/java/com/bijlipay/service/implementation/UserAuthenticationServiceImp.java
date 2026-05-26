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

    @Override
    public UserAuthentication getUser(long l) {
        UserAuthentication username = userAuthenticateRepo.getuser(l);
        return username;
    }

    @Override
    public void insertdata(String jwtToken, User userData) {
        UserAuthentication userAuthentication = new UserAuthentication();
        userAuthentication.setToken(jwtToken);
        userAuthentication.setUser(userData);
        userAuthenticateRepo.save(userAuthentication);
    }

    @Override
    public void update(String jwtToken, Long long1) {
        userAuthenticateRepo.update(jwtToken, long1);
    }
}
