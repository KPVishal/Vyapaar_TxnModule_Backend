package com.bijlipay.model.dto.notification;

import java.util.Arrays;

public class BulkNotificationDto {
    Long[] notificationIds;

    public Long[] getNotificationIds() {
        return this.notificationIds;
    }

    public void setNotificationIds(Long[] notificationIds) {
        this.notificationIds = notificationIds;
    }

    public String toString() {
        return "BulkNotificationDto{notificationIds=" + Arrays.toString(this.notificationIds) + "}";
    }
}