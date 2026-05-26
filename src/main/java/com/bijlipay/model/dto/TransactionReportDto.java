package com.bijlipay.model.dto;

import java.util.Arrays;
import java.util.List;

public class TransactionReportDto {

    private String txnDate;
    private String mid;
    private String payeeVpaOrTid;
    private String responseCodeOrTxnType;
    private String bankRrn;
    private String txnAmount;

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getPayeeVpaOrTid() {
        return payeeVpaOrTid;
    }

    public void setPayeeVpaOrTid(String payeeVpaOrTid) {
        this.payeeVpaOrTid = payeeVpaOrTid;
    }

    public String getResponseCodeOrTxnType() {
        return responseCodeOrTxnType;
    }

    public void setResponseCodeOrTxnType(String responseCodeOrTxnType) {
        this.responseCodeOrTxnType = responseCodeOrTxnType;
    }

    public String getBankRrn() {
        return bankRrn;
    }

    public void setBankRrn(String bankRrn) {
        this.bankRrn = bankRrn;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public List<String> getAsList() {
        return Arrays.asList(txnDate, mid, payeeVpaOrTid, responseCodeOrTxnType, bankRrn, txnAmount);
    }
}
