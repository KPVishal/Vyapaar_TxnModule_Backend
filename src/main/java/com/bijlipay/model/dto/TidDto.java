package com.bijlipay.model.dto;

public class TidDto {
    private String tid;
    private String mid;

    private boolean isQR;

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public boolean isQR() {
        return isQR;
    }

    public void setQR(boolean QR) {
        isQR = QR;
    }

    public TidDto(String tid, String mid, boolean isQR) {
        this.tid = tid;
        this.mid = mid;
        this.isQR = isQR;
    }
}
