package com.bijlipay.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

@Component
public class JupiterTxnData {
    @JsonProperty("mti")
    private String mti;
    @JsonProperty("txnType")
    private String txnType;
    @JsonProperty("terminalId")
    private String terminalId;
    @JsonProperty("merchantId")
    private String merchantId;
    @JsonProperty("txnDate")
    private String txnDate;
    @JsonProperty("txnTime")
    private String txnTime;
    @JsonProperty("txnAmount")
    private String txnAmount;
    @JsonProperty("txnResponseCode")
    private String txnResponseCode;
    @JsonProperty("responseReceivedTime")
    private String responseReceivedTime;
    @JsonProperty("rrn")
    private String rrn;
    @JsonProperty("stan")
    private String stan;
    @JsonProperty("invoiceNumber")
    private String invoiceNumber;
    @JsonProperty("batchNumber")
    private String batchNumber;
    @JsonProperty("txnAdditionalAmount")
    private String txnAdditionalAmount;
    @JsonProperty("maskedCardNumber")
    private String maskedCardNumber;
    @JsonProperty("authCode")
    private String authCode;

    public String getMti() {
        return this.mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getTxnType() {
        return this.txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getTerminalId() {
        return this.terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTxnDate() {
        return this.txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public String getTxnTime() {
        return this.txnTime;
    }

    public void setTxnTime(String txnTime) {
        this.txnTime = txnTime;
    }

    public String getTxnAmount() {
        return this.txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getTxnResponseCode() {
        return this.txnResponseCode;
    }

    public void setTxnResponseCode(String txnResponseCode) {
        this.txnResponseCode = txnResponseCode;
    }

    public String getResponseReceivedTime() {
        return this.responseReceivedTime;
    }

    public void setResponseReceivedTime(String responseReceivedTime) {
        this.responseReceivedTime = responseReceivedTime;
    }

    public String getRrn() {
        return this.rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getStan() {
        return this.stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getBatchNumber() {
        return this.batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getTxnAdditionalAmount() {
        return this.txnAdditionalAmount;
    }

    public void setTxnAdditionalAmount(String txnAdditionalAmount) {
        this.txnAdditionalAmount = txnAdditionalAmount;
    }

    public String getMaskedCardNumber() {
        return this.maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getAuthCode() {
        return this.authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String toString() {
        return "JupiterTxnData{mti='" + this.mti + "', txnType='" + this.txnType + "', terminalId='" + this.terminalId + "', merchantId='" + this.merchantId + "', txnDate='" + this.txnDate + "', txnTime='" + this.txnTime + "', txnAmount='" + this.txnAmount + "', txnResponseCode='" + this.txnResponseCode + "', responseReceivedTime=" + this.responseReceivedTime + ", rrn='" + this.rrn + "', stan='" + this.stan + "', invoiceNumber='" + this.invoiceNumber + "', batchNumber='" + this.batchNumber + "', txnAdditionalAmount='" + this.txnAdditionalAmount + "', maskedCardNumber='" + this.maskedCardNumber + "', authCode='" + this.authCode + "'}";
    }
}