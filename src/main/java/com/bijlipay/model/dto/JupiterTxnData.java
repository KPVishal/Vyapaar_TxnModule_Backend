package com.bijlipay.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

import java.util.Date;

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
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public String getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(String txnTime) {
        this.txnTime = txnTime;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getTxnResponseCode() {
        return txnResponseCode;
    }

    public void setTxnResponseCode(String txnResponseCode) {
        this.txnResponseCode = txnResponseCode;
    }

    public String getResponseReceivedTime() {
        return responseReceivedTime;
    }

    public void setResponseReceivedTime(String responseReceivedTime) {
        this.responseReceivedTime = responseReceivedTime;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getTxnAdditionalAmount() {
        return txnAdditionalAmount;
    }

    public void setTxnAdditionalAmount(String txnAdditionalAmount) {
        this.txnAdditionalAmount = txnAdditionalAmount;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    @Override
    public String toString() {
        return "JupiterTxnData{" +
                "mti='" + mti + '\'' +
                ", txnType='" + txnType + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", txnDate='" + txnDate + '\'' +
                ", txnTime='" + txnTime + '\'' +
                ", txnAmount='" + txnAmount + '\'' +
                ", txnResponseCode='" + txnResponseCode + '\'' +
                ", responseReceivedTime=" + responseReceivedTime +
                ", rrn='" + rrn + '\'' +
                ", stan='" + stan + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", txnAdditionalAmount='" + txnAdditionalAmount + '\'' +
                ", maskedCardNumber='" + maskedCardNumber + '\'' +
                ", authCode='" + authCode + '\'' +
                '}';
    }
}

