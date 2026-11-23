package com.bijlipay.model.dto;

public class SettlementReportQrDto {
    private String vpa;
    private String fromDate;
    private String toDate;
    private String uniqueId;
    private String fileType;

    public String getVpa() {
        return this.vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getFromDate() {
        return this.fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return this.toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getFileType() {
        return this.fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String toString() {
        return "SettlementReportQrDto{vpa='" + this.vpa + "', fromDate='" + this.fromDate + "', toDate='" + this.toDate + "', uniqueId='" + this.uniqueId + "', fileType='" + this.fileType + "'}";
    }
}