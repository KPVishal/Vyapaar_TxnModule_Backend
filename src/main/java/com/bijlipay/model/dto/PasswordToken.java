package com.bijlipay.model.dto;

public class PasswordToken {
    private long userId;
    private int oneTimePassword;
    private boolean used;

    public boolean isUsed() {
        return this.used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public long getUserId() {
        return this.userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getOneTimePassword() {
        return this.oneTimePassword;
    }

    public void setOneTimePassword(int oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }
}