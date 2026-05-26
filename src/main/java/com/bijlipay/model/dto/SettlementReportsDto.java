package com.bijlipay.model.dto;

public class SettlementReportsDto {
    private String merchantId;
    private String fromDate;
    private String toDate; //dd-MM-yyyy

    private String terminalId;

    private String uniqueId;
    private String fileType;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "SettlementReportsDto{" +
                "merchantId='" + merchantId + '\'' +
                ", fromDate='" + fromDate + '\'' +
                ", toDate='" + toDate + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", fileType='" + fileType + '\'' +
                '}';
    }
}
