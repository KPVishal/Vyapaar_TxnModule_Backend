package com.bijlipay.model.dto;

public class TidDto {
    private String tid;
    private String mid;
    private boolean isQR;

    public String getTid() {
        return this.tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getMid() {
        return this.mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public boolean isQR() {
        return this.isQR;
    }

    public void setQR(boolean QR) {
        this.isQR = QR;
    }

    public TidDto(String tid, String mid, boolean isQR) {
        this.tid = tid;
        this.mid = mid;
        this.isQR = isQR;
    }
}