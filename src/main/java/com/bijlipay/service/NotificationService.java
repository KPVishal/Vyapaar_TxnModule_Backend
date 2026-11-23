package com.bijlipay.service;

import com.bijlipay.model.User;
import com.bijlipay.model.dto.notification.BulkNotificationDto;
import com.bijlipay.model.dto.notification.NotificationResponseDto;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void markNotification(long notification, boolean action);

    void deleteNotifications();

    Page<NotificationResponseDto> getNotificationForToday(User currentUser, Optional<String> notificationType, Pageable pageable);

    Long getNotificationCountForToday(User currentuser);

    void markBulkNotification(User user, String notificationType, BulkNotificationDto bulkNotificationDto);
}