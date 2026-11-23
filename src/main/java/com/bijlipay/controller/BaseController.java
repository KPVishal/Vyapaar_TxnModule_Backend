package com.bijlipay.controller;

import com.bijlipay.model.Category;
import com.bijlipay.model.Notification;
import com.bijlipay.model.User;
import com.bijlipay.model.dto.notification.NotificationBodyDto;
import com.bijlipay.model.dto.notification.NotificationDto;
import com.bijlipay.model.dto.notification.PushNotificationResponseDto;
import com.bijlipay.repository.NotificationRepository;
import com.bijlipay.security.UserPrincipal;
import com.bijlipay.service.AndroidPushNotificationsService;
import com.google.gson.Gson;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class BaseController {
    private final Logger logger = LoggerFactory.getLogger(BaseController.class);
    @Autowired
    AndroidPushNotificationsService androidPushNotificationsService;
    @Autowired
    private NotificationRepository notificationRepository;
    @Value("${chargeslip.datasource.url}")
    private String chargeSlipDbUrl;
    @Value("${chargeslip.datasource.username}")
    private String chargeSlipUsername;
    @Value("${chargeslip.datasource.password}")
    private String getChargeSlipPassword;
    @Value("${tsdb.datasource.url}")
    private String tsdbUrl;
    @Value("${tsdb.datasource.username}")
    private String tsdbUserName;
    @Value("${tsdb.datasource.password}")
    private String tsdbPassword;

    public UserPrincipal getSessionUser() {
        this.logger.info("GET SESSION USER : method called.!");
        return (UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public long getSessionUserId() {
        this.logger.info("GET SESSION USER ID : method called.!");
        return ((UserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    @Async("asyncExecutor")
    public void sendPushNotificationGenerate(String title, String reason, Category catInfo, String redirectTo, User userToNotify) {
        Gson gson = new Gson();
        PushNotificationResponseDto<NotificationBodyDto> pushNotificationResponseDto = new PushNotificationResponseDto<NotificationBodyDto>();
        pushNotificationResponseDto.setNotification(new NotificationDto(title, reason));
        NotificationBodyDto leadDto = new NotificationBodyDto();
        leadDto.setCategoryId(catInfo.getId());
        leadDto.setRedirectTo(redirectTo);
        leadDto.setCategoryType(catInfo.getCategoryType());
        leadDto.setMessage(catInfo.getDescription());
        leadDto.setTid(catInfo.getUserTidVpaData().getTid());
        leadDto.setSettlementUrl(catInfo.getFileUrl());
        pushNotificationResponseDto.setToken(userToNotify != null ? userToNotify.getDeviceToken() : null);
        Notification notification = new Notification();
        notification.setNotificationTitle(title);
        notification.setNotificationReason(reason);
        notification.setNotificationRedirectTo(redirectTo);
        notification.setUser(userToNotify);
        notification.setData((new Gson()).toJson(leadDto));
        Notification afterNotificationSave = (Notification)this.notificationRepository.save(notification);
        leadDto.setNotificationId(afterNotificationSave.getId());
        String pushData = gson.toJson(leadDto);
        Map<String, String> dataElement = new HashMap();
        dataElement.put("keysandvalues", pushData);
        pushNotificationResponseDto.setData(dataElement);

        try {
            Map<String, PushNotificationResponseDto> result = new HashMap();
            result.put("message", pushNotificationResponseDto);
            String payload = gson.toJson(result);
            this.sendPushNotification(payload);
        } catch (InterruptedException | ExecutionException e) {
            ((Exception)e).printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String sendPushNotification(String bodyRequest) throws ExecutionException, InterruptedException, IOException {
        CompletableFuture<String> pushNotification = this.androidPushNotificationsService.send(bodyRequest);
        return (String)pushNotification.get();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.chargeSlipDbUrl, this.chargeSlipUsername, this.getChargeSlipPassword);
    }

    public Connection getTSDBConnection() throws SQLException {
        return DriverManager.getConnection(this.tsdbUrl, this.tsdbUserName, this.tsdbPassword);
    }
}