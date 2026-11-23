package com.bijlipay.model.dto.notification;

import com.bijlipay.model.Notification;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class NotificationResponseDto extends Notification {
    private NotificationBodyDto notificationData;

    public NotificationResponseDto(Notification other) {
        this.id = other.getId();
        this.notificationTitle = other.getNotificationTitle();
        this.notificationReason = other.getNotificationReason();
        this.notificationRedirectTo = other.getNotificationRedirectTo();
        this.notificationSeen = other.isNotificationSeen();
        this.notificationDate = other.getNotificationDate();
        this.setNotificationData(other.getData());
    }

    public NotificationBodyDto getNotificationData() {
        return this.notificationData;
    }

    public void setNotificationData(NotificationBodyDto notificationData) {
        this.notificationData = notificationData;
    }

    public void setNotificationData(String data) {
        if (data != null) {
            this.notificationData = (NotificationBodyDto)(new Gson()).fromJson((new JsonParser()).parse(data), NotificationBodyDto.class);
        }

    }
}