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
        return categoryType;
    }

    public void setCategoryType(Integer categoryType) {
        this.categoryType = categoryType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
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

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getSettlementUrl() {
        return settlementUrl;
    }

    public void setSettlementUrl(String settlementUrl) {
        this.settlementUrl = settlementUrl;
    }

    @Override
    public String toString() {
        return "NotificationBodyDto{" +
                "redirectTo='" + redirectTo + '\'' +
                ", tid='" + tid + '\'' +
                ", categoryId=" + categoryId +
                ", categoryType=" + categoryType +
                ", message='" + message + '\'' +
                ", notificationId=" + notificationId +
                ", vpa='" + vpa + '\'' +
                ", settlementUrl='" + settlementUrl + '\'' +
                '}';
    }
}
