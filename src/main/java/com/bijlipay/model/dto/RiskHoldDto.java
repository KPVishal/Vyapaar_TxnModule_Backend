package com.bijlipay.model.dto;

import java.util.Date;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

public class RiskHoldDto {
    private String mid;
    private String tid;
    private String cardNumber;
    private String cardType;
    private String txnType;
    private String rrn;
    @Temporal(TemporalType.TIMESTAMP)
    private Date txnDate;
    private String txnAmount;
    @Temporal(TemporalType.TIMESTAMP)
    private Date holdDate;
    private String holdRemark;
    private String network;
    private String status;
    private String riskHoldId;

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

    public String getCardNumber() {
        return this.cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardType() {
        return this.cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getTxnType() {
        return this.txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getRrn() {
        return this.rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public Date getTxnDate() {
        return this.txnDate;
    }

    public void setTxnDate(Date txnDate) {
        this.txnDate = txnDate;
    }

    public String getTxnAmount() {
        return this.txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public Date getHoldDate() {
        return this.holdDate;
    }

    public void setHoldDate(Date holdDate) {
        this.holdDate = holdDate;
    }

    public String getHoldRemark() {
        return this.holdRemark;
    }

    public void setHoldRemark(String holdRemark) {
        this.holdRemark = holdRemark;
    }

    public String getNetwork() {
        return this.network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRiskHoldId() {
        return this.riskHoldId;
    }

    public void setRiskHoldId(String riskHoldId) {
        this.riskHoldId = riskHoldId;
    }

    public String toString() {
        String var10000 = this.mid;
        return "RiskHoldDto{mid='" + var10000 + "', tid='" + this.tid + "', cardNumber='" + this.cardNumber + "', cardType='" + this.cardType + "', txnType='" + this.txnType + "', rrn='" + this.rrn + "', txnDate=" + String.valueOf(this.txnDate) + ", txnAmount='" + this.txnAmount + "', holdDate=" + String.valueOf(this.holdDate) + ", holdRemark='" + this.holdRemark + "', network='" + this.network + "', status='" + this.status + "', riskHoldId='" + this.riskHoldId + "'}";
    }
}