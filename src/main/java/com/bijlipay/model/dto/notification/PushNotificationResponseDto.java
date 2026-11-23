package com.bijlipay.model.dto.notification;

public class PushNotificationResponseDto<T> {
    private String token;
    private NotificationDto notification;
    private Object data;

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public NotificationDto getNotification() {
        return this.notification;
    }

    public void setNotification(NotificationDto notification) {
        this.notification = notification;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String toString() {
        String var10000 = this.token;
        return "PushNotificationResponseDto{token='" + var10000 + "', notification=" + String.valueOf(this.notification) + ", data=" + String.valueOf(this.data) + "}";
    }
}
