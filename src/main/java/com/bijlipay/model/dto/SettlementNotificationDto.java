package com.bijlipay.model.dto;

public class SettlementNotificationDto {
    private String title;
    private String mid;
    private String tid;
    private String settlementFileUrl;
    private String category;
    private String vpa;
    private String uniqueId;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return this.tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getMid() {
        return this.mid;
    }

    public String getSettlementFileUrl() {
        return this.settlementFileUrl;
    }

    public void setSettlementFileUrl(String settlementFileUrl) {
        this.settlementFileUrl = settlementFileUrl;
    }

    public String getVpa() {
        return this.vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String toString() {
        return "SettlementNotificationDto{title='" + this.title + "', mid='" + this.mid + "', tid='" + this.tid + "', settlementFileUrl='" + this.settlementFileUrl + "', category='" + this.category + "', vpa='" + this.vpa + "', uniqueId='" + this.uniqueId + "'}";
    }
}