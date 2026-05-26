package com.bijlipay.service.implementation;

import com.bijlipay.model.Notification;
import com.bijlipay.model.User;
import com.bijlipay.model.dto.notification.BulkNotificationDto;
import com.bijlipay.model.dto.notification.NotificationResponseDto;
import com.bijlipay.repository.NotificationRepository;
import com.bijlipay.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImp implements NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationServiceImp.class);
    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void markNotification(long id, boolean action) {
        Optional<Notification> notification = notificationRepository.findById(id);
        if (notification.isPresent()) {
            Notification updateSeen = notification.get();
            updateSeen.setNotificationSeen(action);
            notificationRepository.save(updateSeen);
        }
    }

    @Override
    public void markBulkNotification(User user, String notificationType,BulkNotificationDto bulkNotificationDto) {
//        if (bulkNotificationDto != null) {
//            if (bulkNotificationDto.getNotificationIds().length > 0) {
//                List<String> ids = Arrays.stream(bulkNotificationDto.getNotificationIds()).map(String::valueOf).collect(Collectors.toList());
//                logger.info("The ids : ---------- : {}", ids);
//                notificationRepository.updateBulkNotifications(ids);
//            }
//        }
        notificationRepository.updateBulkNotificationsBasedOnType(user,notificationType);
    }

    @Override
    public Page<NotificationResponseDto> getNotificationForToday(User currentUser, Optional<String> notificationType, Pageable pageable) {

        List<NotificationResponseDto> notificationResponseDtos = new ArrayList<>();
        Page<Notification> notifications = null;
        if (notificationType.isPresent()) {
            notifications = notificationRepository.findAllNotificationsForToday(currentUser, notificationType.get(), pageable);
        } else {
            notifications = notificationRepository.findAllNotifications(currentUser, pageable);
        }
        notificationResponseDtos = notifications.stream().map(noti -> {
            return new NotificationResponseDto(noti);
        }).collect(Collectors.toList());
        return new PageImpl<>(notificationResponseDtos, PageRequest.of(notifications.getNumber(), notifications.getSize(), notifications.getSort()), notifications.getTotalElements());
    }

    @Override
    public Long getNotificationCountForToday(User currentuser) {
        return notificationRepository.findReadNotificationsCountForToday(currentuser);
    }

    @Override
    public void deleteNotifications() {
        notificationRepository.deleteNotifications();
    }
}
