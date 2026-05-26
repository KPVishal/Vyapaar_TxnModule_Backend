package com.bijlipay.model.dto;

public class SettlementReportQrDto {

    private String vpa;
    private String fromDate;
    private String toDate; //dd-MM-yyyy

    private String uniqueId;
    private String fileType;


    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
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
        return "SettlementReportQrDto{" +
                "vpa='" + vpa + '\'' +
                ", fromDate='" + fromDate + '\'' +
                ", toDate='" + toDate + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", fileType='" + fileType + '\'' +
                '}';
    }
}
