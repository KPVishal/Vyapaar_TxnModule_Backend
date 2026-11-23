package com.bijlipay.model.dto;

public class VpaDto {
    private String vpa;
    private boolean isQR;

    public String getVpa() {
        return this.vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public boolean isQR() {
        return this.isQR;
    }

    public void setQR(boolean QR) {
        this.isQR = QR;
    }

    public VpaDto(String vpa, boolean isQR) {
        this.vpa = vpa;
        this.isQR = isQR;
    }
}