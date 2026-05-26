package com.bijlipay.model.dto.notification;

import java.util.Arrays;

public class SettlementNotificationUpdate {
    private String title;
    //    private String body;
    private String[] mid;
    private String category;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getMid() {
        return mid;
    }

    public void setMid(String[] mid) {
        this.mid = mid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "SettlementNotificationDto{" + "title='" + title + '\'' + ", mid=" + Arrays.toString(mid) + ", category='" + category + '\'' + '}';
    }
}
