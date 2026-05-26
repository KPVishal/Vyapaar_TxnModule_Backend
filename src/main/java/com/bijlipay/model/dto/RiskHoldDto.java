package com.bijlipay.model.dto;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

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

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public Date getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(Date txnDate) {
        this.txnDate = txnDate;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
    }

    public Date getHoldDate() {
        return holdDate;
    }

    public void setHoldDate(Date holdDate) {
        this.holdDate = holdDate;
    }

    public String getHoldRemark() {
        return holdRemark;
    }

    public void setHoldRemark(String holdRemark) {
        this.holdRemark = holdRemark;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRiskHoldId() {
        return riskHoldId;
    }

    public void setRiskHoldId(String riskHoldId) {
        this.riskHoldId = riskHoldId;
    }

    @Override
    public String toString() {
        return "RiskHoldDto{" + "mid='" + mid + '\'' + ", tid='" + tid + '\'' + ", cardNumber='" + cardNumber + '\'' + ", cardType='" + cardType + '\'' + ", txnType='" + txnType + '\'' + ", rrn='" + rrn + '\'' + ", txnDate=" + txnDate + ", txnAmount='" + txnAmount + '\'' + ", holdDate=" + holdDate + ", holdRemark='" + holdRemark + '\'' + ", network='" + network + '\'' + ", status='" + status + '\'' + ", riskHoldId='" + riskHoldId + '\'' + '}';
    }
}
