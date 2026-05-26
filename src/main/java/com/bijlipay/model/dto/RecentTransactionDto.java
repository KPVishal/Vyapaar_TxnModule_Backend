package com.bijlipay.model.dto;

public class RecentTransactionDto {
    private String rrn;
    private String amount;
    private String transactionTime;
    private String status;

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }


    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RecentTransactionDto(String rrn, String amount, String transactionTime, String status) {
        this.rrn = rrn;
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.status = status;
    }
}
