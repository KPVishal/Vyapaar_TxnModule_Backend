package com.bijlipay.model.dto.notification;

import java.util.Arrays;

public class SettlementNotificationUpdate {
    private String title;
    private String[] mid;
    private String category;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getMid() {
        return this.mid;
    }

    public void setMid(String[] mid) {
        this.mid = mid;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String toString() {
        String var10000 = this.title;
        return "SettlementNotificationDto{title='" + var10000 + "', mid=" + Arrays.toString(this.mid) + ", category='" + this.category + "'}";
    }
}