package com.bijlipay.model.dto;

public class SettlementReportsDto {
    private String merchantId;
    private String fromDate;
    private String toDate;
    private String terminalId;
    private String uniqueId;
    private String fileType;

    public String getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
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

    public String getTerminalId() {
        return this.terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
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
        return "SettlementReportsDto{merchantId='" + this.merchantId + "', fromDate='" + this.fromDate + "', toDate='" + this.toDate + "', terminalId='" + this.terminalId + "', uniqueId='" + this.uniqueId + "', fileType='" + this.fileType + "'}";
    }
}