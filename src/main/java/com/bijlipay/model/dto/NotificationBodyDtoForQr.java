package com.bijlipay.model.dto;

public class NotificationBodyDtoForQr {
    private String redirectTo;
    private String vpa;
    private Long vpaId;
    private String message;
    private Long notificationId;

    public String getRedirectTo() {
        return redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public Long getVpaId() {
        return vpaId;
    }

    public void setVpaId(Long vpaId) {
        this.vpaId = vpaId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
}
