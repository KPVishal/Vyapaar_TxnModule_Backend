package com.bijlipay.model.dto.notification;

import java.util.Arrays;

public class BulkNotificationDto {
    Long[] notificationIds;

    public Long[] getNotificationIds() {
        return notificationIds;
    }

    public void setNotificationIds(Long[] notificationIds) {
        this.notificationIds = notificationIds;
    }

    @Override
    public String toString() {
        return "BulkNotificationDto{" + "notificationIds=" + Arrays.toString(notificationIds) + '}';
    }
}
