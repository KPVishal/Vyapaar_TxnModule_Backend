package com.bijlipay.repository;

import com.bijlipay.model.Notification;
import com.bijlipay.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends CrudRepository<Notification, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE notifications SET notification_seen = 1 WHERE notification_date < NOW() - INTERVAL 1 DAY", nativeQuery = true)
    void deleteNotifications();

    @Query("SELECT n FROM Notification n WHERE n.user = ?1 and notificationSeen = FALSE AND notificationRedirectTo =?2")
    Page<Notification> findAllNotificationsForToday(User currentUser, String notificationType, Pageable pageable);

    @Query("SELECT COUNT(*) FROM Notification n WHERE n.user = ?1 AND HOUR(TIMEDIFF(NOW(), n.notificationDate)) <= 24 AND n.notificationSeen=0")
    Long findReadNotificationsCountForToday(User currentUser);

    @Modifying
    @Transactional
    @Query(value = "UPDATE notifications SET notification_seen = 1 WHERE notification_id IN ?1", nativeQuery = true)
    void updateBulkNotifications(List<String> ids);

    @Query("SELECT n FROM Notification n WHERE n.user = ?1 and notificationSeen = FALSE ")
    Page<Notification> findAllNotifications(User currentUser, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "UPDATE notifications SET notification_seen = 1 WHERE user_id =?1 AND notification_redirect_to=?2", nativeQuery = true)
    void updateBulkNotificationsBasedOnType(User user, String notificationType);
}
