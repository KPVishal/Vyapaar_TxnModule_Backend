package com.bijlipay.model.dto;

public class VpaDto {
    private String vpa;

    private boolean isQR;

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public boolean isQR() {
        return isQR;
    }

    public void setQR(boolean QR) {
        isQR = QR;
    }

    public VpaDto(String vpa, boolean isQR) {
        this.vpa = vpa;
        this.isQR = isQR;
    }
}
