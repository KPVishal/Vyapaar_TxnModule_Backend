package com.bijlipay.service.implementation;

import com.bijlipay.model.Notification;
import com.bijlipay.model.User;
import com.bijlipay.model.dto.notification.BulkNotificationDto;
import com.bijlipay.model.dto.notification.NotificationResponseDto;
import com.bijlipay.repository.NotificationRepository;
import com.bijlipay.service.NotificationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImp implements NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationServiceImp.class);
    @Autowired
    private NotificationRepository notificationRepository;

    public void markNotification(long id, boolean action) {
        Optional<Notification> notification = this.notificationRepository.findById(id);
        if (notification.isPresent()) {
            Notification updateSeen = (Notification)notification.get();
            updateSeen.setNotificationSeen(action);
            this.notificationRepository.save(updateSeen);
        }

    }

    public void markBulkNotification(User user, String notificationType, BulkNotificationDto bulkNotificationDto) {
        this.notificationRepository.updateBulkNotificationsBasedOnType(user, notificationType);
    }

    public Page<NotificationResponseDto> getNotificationForToday(User currentUser, Optional<String> notificationType, Pageable pageable) {
        new ArrayList();
        Page<Notification> notifications = null;
        if (notificationType.isPresent()) {
            notifications = this.notificationRepository.findAllNotificationsForToday(currentUser, (String)notificationType.get(), pageable);
        } else {
            notifications = this.notificationRepository.findAllNotifications(currentUser, pageable);
        }

        List<NotificationResponseDto> notificationResponseDtos = (List)notifications.stream().map((noti) -> new NotificationResponseDto(noti)).collect(Collectors.toList());
        return new PageImpl(notificationResponseDtos, PageRequest.of(notifications.getNumber(), notifications.getSize(), notifications.getSort()), notifications.getTotalElements());
    }

    public Long getNotificationCountForToday(User currentuser) {
        return this.notificationRepository.findReadNotificationsCountForToday(currentuser);
    }

    public void deleteNotifications() {
        this.notificationRepository.deleteNotifications();
    }
}