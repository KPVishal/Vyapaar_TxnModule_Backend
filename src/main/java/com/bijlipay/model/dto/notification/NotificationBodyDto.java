package com.bijlipay.model.dto.notification;

public class NotificationBodyDto {
    private String redirectTo;
    private String tid;
    private Long categoryId;
    private Integer categoryType;
    private String message;
    private Long notificationId;
    private String vpa;
    private String settlementUrl;

    public Integer getCategoryType() {
        return this.categoryType;
    }

    public void setCategoryType(Integer categoryType) {
        this.categoryType = categoryType;
    }

    public Long getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getRedirectTo() {
        return this.redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }

    public String getTid() {
        return this.tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
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

    public String getVpa() {
        return this.vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getSettlementUrl() {
        return this.settlementUrl;
    }

    public void setSettlementUrl(String settlementUrl) {
        this.settlementUrl = settlementUrl;
    }

    public String toString() {
        return "NotificationBodyDto{redirectTo='" + this.redirectTo + "', tid='" + this.tid + "', categoryId=" + this.categoryId + ", categoryType=" + this.categoryType + ", message='" + this.message + "', notificationId=" + this.notificationId + ", vpa='" + this.vpa + "', settlementUrl='" + this.settlementUrl + "'}";
    }
}