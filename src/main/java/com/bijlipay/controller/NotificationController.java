package com.bijlipay.controller;

import com.bijlipay.config.WebConfig;
import com.bijlipay.model.User;
import com.bijlipay.model.dto.notification.BulkNotificationDto;
import com.bijlipay.model.dto.notification.NotificationResponseDto;
import com.bijlipay.repository.NotificationRepository;
import com.bijlipay.repository.UserRepository;
import com.bijlipay.security.UserPrincipal;
import com.bijlipay.service.NotificationService;
import com.bijlipay.service.UserTidVpaDataService;
import com.bijlipay.util.ApiResponse;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/txn")
public class NotificationController extends BaseController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserTidVpaDataService userTidVpaDataService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private WebConfig webConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Calcutta")
    public void removeNotification() {
        notificationRepository.deleteNotifications();
        LOGGER.info("AUTO REMOVE NOTIFICATIONS : Notifications older than 24 hours cleared!");
    }

    @GetMapping(value = {"/notification", "/notification/{notificationType}"})
    public ResponseEntity<?> getAllNotificationsForToday(@PathVariable("notificationType") Optional<String> notificationType, @RequestParam Map<String, String> requestParams, Pageable pageable) {
        LOGGER.info("Start to GET ALL NOTIFICATIONS : api called ----:/notification/today");
        pageable = webConfig.resolvePageable(requestParams, pageable);
        Page<NotificationResponseDto> notifications = notificationService.getNotificationForToday(userTidVpaDataService.getUserById(getSessionUser().getId()), notificationType, pageable);
        LOGGER.info("End For GET ALL NOTIFICATIONS : ----:/notification/today");
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", notifications), HttpStatus.OK);
    }

    @GetMapping("/notification/{id}/{action}")
    public ResponseEntity<?> markNotification(@PathVariable("id") long notification, @PathVariable("action") boolean action) {
        LOGGER.info("Start to MARK NOTIFICATION : api called : /notification/{id}/{action}---- : {}, action : ---- : {}", notification, action);
        notificationService.markNotification(notification, action);
        LOGGER.info("End For MARK NOTIFICATION API ----/notification/{id}/{action}");
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", "updated"), HttpStatus.OK);
    }

//    @PostMapping("/notification-bulk-update")
//    public ResponseEntity<?> markBulkNotification(@RequestBody BulkNotificationDto bulkNotificationDto) {
//        Gson gson = new Gson();
//        String requestDto = gson.toJson(bulkNotificationDto);
//        LOGGER.info("Start to MARK BULK NOTIFICATION : Api called.! :/notification-bulk-update ---- : {}", requestDto);
//        notificationService.markBulkNotification(bulkNotificationDto);
//        LOGGER.info("End For MARK BULK NOTIFICATION Api :/notification-bulk-update ---- ");
//        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", "updated"), HttpStatus.OK);
//    }
    @PostMapping("/notification-bulk-update/{notificationType}")
    public ResponseEntity<?> markBulkNotification(@PathVariable("notificationType") String notificationType,@RequestBody BulkNotificationDto bulkNotificationDto) {
        Gson gson = new Gson();
        String requestDto = gson.toJson(bulkNotificationDto);
        UserPrincipal user = getSessionUser();
        LOGGER.info("Start to MARK BULK NOTIFICATION : Api called.! :/notification-bulk-update ---- : {}", requestDto);
        notificationService.markBulkNotification(userTidVpaDataService.getUserById(getSessionUser().getId()),notificationType,bulkNotificationDto);
        LOGGER.info("End For MARK BULK NOTIFICATION Api :/notification-bulk-update ---- ");
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", "updated"), HttpStatus.OK);
    }
}
