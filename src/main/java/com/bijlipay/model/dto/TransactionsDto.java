package com.bijlipay.model.dto;

public class TransactionsDto {
    private String mid;
    private String tid;
    private String cardNo;
    private String bankName;
    private String cardType;
    private String city;
    private String email;
    private String ifsc;
    private String merchantName;
    private Double netAmount;
    private String paymentDate;
    private String rrn;
    private String txnDate;
    private Double txnAmount;
    private String txnType;

    public String getTxnType() {
        return this.txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getMid() {
        return this.mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return this.tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getBankName() {
        return this.bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCardType() {
        return this.cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIfsc() {
        return this.ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getMerchantName() {
        return this.merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public Double getNetAmount() {
        return this.netAmount;
    }

    public void setNetAmount(Double netAmount) {
        this.netAmount = netAmount;
    }

    public String getPaymentDate() {
        return this.paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getRrn() {
        return this.rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getTxnDate() {
        return this.txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public Double getTxnAmount() {
        return this.txnAmount;
    }

    public void setTxnAmount(Double txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String toString() {
        return "TransactionsDto{mid='" + this.mid + "', tid='" + this.tid + "', cardNo='" + this.cardNo + "', bankName='" + this.bankName + "', cardType='" + this.cardType + "', city='" + this.city + "', email='" + this.email + "', ifsc='" + this.ifsc + "', merchantName='" + this.merchantName + "', netAmount=" + this.netAmount + ", paymentDate=" + this.paymentDate + ", rrn='" + this.rrn + "', txnDate=" + this.txnDate + ", txnAmount=" + this.txnAmount + "}";
    }
}