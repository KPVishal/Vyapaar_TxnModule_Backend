package com.bijlipay.model.dto.notification;

public class NotificationDto {
    private String title;
    private String body;
    private Long categoryId;
    private String tid;

    public NotificationDto(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String toString() {
        return "NotificationDto{title='" + this.title + "', body='" + this.body + "', categoryId=" + this.categoryId + ", tid='" + this.tid + "'}";
    }
}