package com.bijlipay.model.dto;

public class ForgotPasswordEmail extends BaseEmail {
    private String passwordToken;

    public String getPasswordToken() {
        return this.passwordToken;
    }

    public void setPasswordToken(String passwordToken) {
        this.passwordToken = passwordToken;
    }
}