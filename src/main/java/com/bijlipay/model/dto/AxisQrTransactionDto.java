package com.bijlipay.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.List;

public class AxisQrTransactionDto {

    private String customerVpa;
    private String creditVpa;
    private String merchantId;
    private String merchantChannelId;
    private String merchantTransactionId;
    private String transactionTimestamp;
    private String transactionAmount;
    private String gatewayTransactionId;
    private String gatewayResponseCode;
    private String gatewayResponseMessage;
    private String rrn;
    private String checksum;
    private String payeractype;
    private String purposeCode;
    private String initMode;

    private String createdAt;

    private String txnMode;

    private String txnStatus;

    public String getCustomerVpa() {
        return customerVpa;
    }

    public void setCustomerVpa(String customerVpa) {
        this.customerVpa = customerVpa;
    }

    public String getCreditVpa() {
        return creditVpa;
    }

    public void setCreditVpa(String creditVpa) {
        this.creditVpa = creditVpa;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantChannelId() {
        return merchantChannelId;
    }

    public void setMerchantChannelId(String merchantChannelId) {
        this.merchantChannelId = merchantChannelId;
    }

    public String getMerchantTransactionId() {
        return merchantTransactionId;
    }

    public void setMerchantTransactionId(String merchantTransactionId) {
        this.merchantTransactionId = merchantTransactionId;
    }

    public String getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public void setTransactionTimestamp(String transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public String getGatewayResponseCode() {
        return gatewayResponseCode;
    }

    public void setGatewayResponseCode(String gatewayResponseCode) {
        this.gatewayResponseCode = gatewayResponseCode;
    }

    public String getGatewayResponseMessage() {
        return gatewayResponseMessage;
    }

    public void setGatewayResponseMessage(String gatewayResponseMessage) {
        this.gatewayResponseMessage = gatewayResponseMessage;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getPayeractype() {
        return payeractype;
    }

    public void setPayeractype(String payeractype) {
        this.payeractype = payeractype;
    }

    public String getPurposeCode() {
        return purposeCode;
    }

    public void setPurposeCode(String purposeCode) {
        this.purposeCode = purposeCode;
    }

    public String getInitMode() {
        return initMode;
    }

    public void setInitMode(String initMode) {
        this.initMode = initMode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTxnMode() {
        return txnMode;
    }

    public void setTxnMode(String txnMode) {
        this.txnMode = txnMode;
    }

    public String getTxnStatus() {
        return txnStatus;
    }

    public void setTxnStatus(String txnStatus) {
        this.txnStatus = txnStatus;
    }

    @JsonIgnore
    public List<String> getAsList() {
        return Arrays.asList(customerVpa, creditVpa, merchantId, merchantChannelId, merchantTransactionId, transactionTimestamp, transactionAmount, gatewayTransactionId, gatewayResponseCode, gatewayResponseMessage, rrn, checksum, payeractype, purposeCode, initMode);
    }
}
