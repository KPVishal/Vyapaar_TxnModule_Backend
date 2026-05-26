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
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public Double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(Double netAmount) {
        this.netAmount = netAmount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public Double getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(Double txnAmount) {
        this.txnAmount = txnAmount;
    }

    @Override
    public String toString() {
        return "TransactionsDto{" + "mid='" + mid + '\'' + ", tid='" + tid + '\'' + ", cardNo='" + cardNo + '\'' + ", bankName='" + bankName + '\'' + ", cardType='" + cardType + '\'' + ", city='" + city + '\'' + ", email='" + email + '\'' + ", ifsc='" + ifsc + '\'' + ", merchantName='" + merchantName + '\'' + ", netAmount=" + netAmount + ", paymentDate=" + paymentDate + ", rrn='" + rrn + '\'' + ", txnDate=" + txnDate + ", txnAmount=" + txnAmount + '}';
    }
}
