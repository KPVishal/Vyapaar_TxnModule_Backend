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
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getMid() {
        return mid;
    }

    public String getSettlementFileUrl() {
        return settlementFileUrl;
    }

    public void setSettlementFileUrl(String settlementFileUrl) {
        this.settlementFileUrl = settlementFileUrl;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String toString() {
        return "SettlementNotificationDto{" +
                "title='" + title + '\'' +
                ", mid='" + mid + '\'' +
                ", tid='" + tid + '\'' +
                ", settlementFileUrl='" + settlementFileUrl + '\'' +
                ", category='" + category + '\'' +
                ", vpa='" + vpa + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                '}';
    }
}
