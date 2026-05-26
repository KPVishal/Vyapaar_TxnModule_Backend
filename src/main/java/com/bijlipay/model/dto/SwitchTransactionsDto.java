package com.bijlipay.model.dto;

import java.math.BigInteger;
import java.util.Date;

public class SwitchTransactionsDto {
    private BigInteger id;
    private String mti;
    private String txnType;
    private String terminalId;
    private String merchantId;
    private String txnDate;
    private String txnTime;
    private Double txnAmount;
    private String txnResponseCode;
    private String responseReceivedTime;
    private String rRNumber;
    private String stan;
    private String invoiceNumber;
    private String batchNumber;
    private Double TxnAdditionalAmount;
    private String maskedCardNumber;
    private String panEntryMode;
    private String bin;
    private String customerVpa;
    private String txnStatus;
    private String txnMode;

    public SwitchTransactionsDto() {
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

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

    public Double getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(Double txnAmount) {
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

    public String getrRNumber() {
        return rRNumber;
    }

    public void setrRNumber(String rRNumber) {
        this.rRNumber = rRNumber;
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

    public Double getTxnAdditionalAmount() {
        return TxnAdditionalAmount;
    }

    public void setTxnAdditionalAmount(Double txnAdditionalAmount) {
        TxnAdditionalAmount = txnAdditionalAmount;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getPanEntryMode() {
        return panEntryMode;
    }

    public void setPanEntryMode(String panEntryMode) {
        this.panEntryMode = panEntryMode;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getCustomerVpa() {
        return customerVpa;
    }

    public void setCustomerVpa(String customerVpa) {
        this.customerVpa = customerVpa;
    }

    public String getTxnStatus() {
        return txnStatus;
    }

    public void setTxnStatus(String txnStatus) {
        this.txnStatus = txnStatus;
    }

    public String getTxnMode() {
        return txnMode;
    }

    public void setTxnMode(String txnMode) {
        this.txnMode = txnMode;
    }
}
