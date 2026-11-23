package com.bijlipay.model.dto;

public class NotificationBodyDtoForQr {
    private String redirectTo;
    private String vpa;
    private Long vpaId;
    private String message;
    private Long notificationId;

    public String getRedirectTo() {
        return this.redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }

    public String getVpa() {
        return this.vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public Long getVpaId() {
        return this.vpaId;
    }

    public void setVpaId(Long vpaId) {
        this.vpaId = vpaId;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getNotificationId() {
        return this.notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
}