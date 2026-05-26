package com.bijlipay.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "risk_hold")
public class RiskHold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;
    @Column(name = "risk_hold_id", nullable = false, unique = true)
    private String riskHoldId;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updatedAt;
    @Column(name = "mid", nullable = true)
    private String mid;
    @Column(name = "tid", nullable = true)
    private String tid;
    @Column(name = "card_number", nullable = true)
    private String cardNumber;
    @Column(name = "card_type", nullable = true)
    private String cardType;
    @Column(name = "txn_type", nullable = true)
    private String txnType;
    @Column(name = "rrn", nullable = true)
    private String rrn;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "txn_date", nullable = true)
    private Date txnDate;
    @Column(name = "txn_amount", nullable = true)
    private String txnAmount;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "hold_date", nullable = true)
    private Date holdDate;
    @Column(name = "hold_remark", nullable = true)
    private String holdRemark;
    @Column(name = "network", nullable = true)
    private String network;
    private String totalAmt;
    @Column(name = "status", nullable = true)
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRiskHoldId() {
        return riskHoldId;
    }

    public void setRiskHoldId(String riskHoldId) {
        this.riskHoldId = riskHoldId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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

//    public void setTxnDate(Date txnDate) {
//        this.txnDate = txnDate;
//    }

    public void setTxnDate(Date rawTxnDate) {
        this.txnDate = rawTxnDate != null ?
                (String.valueOf(rawTxnDate.getTime()).length() == 10 ? new Date(rawTxnDate.getTime() * 1000) : new Date(rawTxnDate.getTime())) :
                null;
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

//    public void setHoldDate(Date holdDate) {
//        this.holdDate = holdDate;
//    }
    public void setHoldDate(Date rawHoldDate) {
        this.holdDate = rawHoldDate != null ?
                (String.valueOf(rawHoldDate.getTime()).length() == 10 ? new Date(rawHoldDate.getTime() * 1000) : new Date(rawHoldDate.getTime())) :
                null;
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

    public String getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(String totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RiskHold{" + "id=" + id + ", riskHoldId='" + riskHoldId + '\'' + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", mid='" + mid + '\'' + ", tid='" + tid + '\'' + ", cardNumber='" + cardNumber + '\'' + ", cardType='" + cardType + '\'' + ", txnType='" + txnType + '\'' + ", rrn='" + rrn + '\'' + ", txnDate=" + txnDate + ", txnAmount='" + txnAmount + '\'' + ", holdDate=" + holdDate + ", holdRemark='" + holdRemark + '\'' + ", network='" + network + '\'' + ", totalAmt='" + totalAmt + '\'' + ", status='" + status + '\'' + '}';
    }
}
