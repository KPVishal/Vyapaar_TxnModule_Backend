package com.bijlipay.model.dto.notification;

public class PushNotificationResponseDto<T> {
    private String token;
    private NotificationDto notification;
    private Object data;

    public PushNotificationResponseDto() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public NotificationDto getNotification() {
        return notification;
    }

    public void setNotification(NotificationDto notification) {
        this.notification = notification;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PushNotificationResponseDto{" +
                "token='" + token + '\'' +
                ", notification=" + notification +
                ", data=" + data +
                '}';
    }
}


