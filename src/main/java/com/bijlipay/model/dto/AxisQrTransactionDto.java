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
        return this.customerVpa;
    }

    public void setCustomerVpa(String customerVpa) {
        this.customerVpa = customerVpa;
    }

    public String getCreditVpa() {
        return this.creditVpa;
    }

    public void setCreditVpa(String creditVpa) {
        this.creditVpa = creditVpa;
    }

    public String getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantChannelId() {
        return this.merchantChannelId;
    }

    public void setMerchantChannelId(String merchantChannelId) {
        this.merchantChannelId = merchantChannelId;
    }

    public String getMerchantTransactionId() {
        return this.merchantTransactionId;
    }

    public void setMerchantTransactionId(String merchantTransactionId) {
        this.merchantTransactionId = merchantTransactionId;
    }

    public String getTransactionTimestamp() {
        return this.transactionTimestamp;
    }

    public void setTransactionTimestamp(String transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }

    public String getTransactionAmount() {
        return this.transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getGatewayTransactionId() {
        return this.gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public String getGatewayResponseCode() {
        return this.gatewayResponseCode;
    }

    public void setGatewayResponseCode(String gatewayResponseCode) {
        this.gatewayResponseCode = gatewayResponseCode;
    }

    public String getGatewayResponseMessage() {
        return this.gatewayResponseMessage;
    }

    public void setGatewayResponseMessage(String gatewayResponseMessage) {
        this.gatewayResponseMessage = gatewayResponseMessage;
    }

    public String getRrn() {
        return this.rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getChecksum() {
        return this.checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getPayeractype() {
        return this.payeractype;
    }

    public void setPayeractype(String payeractype) {
        this.payeractype = payeractype;
    }

    public String getPurposeCode() {
        return this.purposeCode;
    }

    public void setPurposeCode(String purposeCode) {
        this.purposeCode = purposeCode;
    }

    public String getInitMode() {
        return this.initMode;
    }

    public void setInitMode(String initMode) {
        this.initMode = initMode;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTxnMode() {
        return this.txnMode;
    }

    public void setTxnMode(String txnMode) {
        this.txnMode = txnMode;
    }

    public String getTxnStatus() {
        return this.txnStatus;
    }

    public void setTxnStatus(String txnStatus) {
        this.txnStatus = txnStatus;
    }

    @JsonIgnore
    public List<String> getAsList() {
        return Arrays.asList(this.customerVpa, this.creditVpa, this.merchantId, this.merchantChannelId, this.merchantTransactionId, this.transactionTimestamp, this.transactionAmount, this.gatewayTransactionId, this.gatewayResponseCode, this.gatewayResponseMessage, this.rrn, this.checksum, this.payeractype, this.purposeCode, this.initMode);
    }
}