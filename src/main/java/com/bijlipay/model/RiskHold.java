package com.bijlipay.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "risk_hold"
)
public class RiskHold {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id",
            nullable = false
    )
    private long id;
    @Column(
            name = "risk_hold_id",
            nullable = false,
            unique = true
    )
    private String riskHoldId;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(
            name = "created_at"
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd"
    )
    private Date createdAt;
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(
            name = "updated_at"
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd"
    )
    private Date updatedAt;
    @Column(
            name = "mid",
            nullable = true
    )
    private String mid;
    @Column(
            name = "tid",
            nullable = true
    )
    private String tid;
    @Column(
            name = "card_number",
            nullable = true
    )
    private String cardNumber;
    @Column(
            name = "card_type",
            nullable = true
    )
    private String cardType;
    @Column(
            name = "txn_type",
            nullable = true
    )
    private String txnType;
    @Column(
            name = "rrn",
            nullable = true
    )
    private String rrn;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(
            name = "txn_date",
            nullable = true
    )
    private Date txnDate;
    @Column(
            name = "txn_amount",
            nullable = true
    )
    private String txnAmount;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(
            name = "hold_date",
            nullable = true
    )
    private Date holdDate;
    @Column(
            name = "hold_remark",
            nullable = true
    )
    private String holdRemark;
    @Column(
            name = "network",
            nullable = true
    )
    private String network;
    private String totalAmt;
    @Column(
            name = "status",
            nullable = true
    )
    private String status;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRiskHoldId() {
        return this.riskHoldId;
    }

    public void setRiskHoldId(String riskHoldId) {
        this.riskHoldId = riskHoldId;
    }

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

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

    public void setTxnDate(Date rawTxnDate) {
        this.txnDate = rawTxnDate != null ? (String.valueOf(rawTxnDate.getTime()).length() == 10 ? new Date(rawTxnDate.getTime() * 1000L) : new Date(rawTxnDate.getTime())) : null;
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

    public void setHoldDate(Date rawHoldDate) {
        this.holdDate = rawHoldDate != null ? (String.valueOf(rawHoldDate.getTime()).length() == 10 ? new Date(rawHoldDate.getTime() * 1000L) : new Date(rawHoldDate.getTime())) : null;
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

    public String getTotalAmt() {
        return this.totalAmt;
    }

    public void setTotalAmt(String totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toString() {
        long var10000 = this.id;
        return "RiskHold{id=" + var10000 + ", riskHoldId='" + this.riskHoldId + "', createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ", mid='" + this.mid + "', tid='" + this.tid + "', cardNumber='" + this.cardNumber + "', cardType='" + this.cardType + "', txnType='" + this.txnType + "', rrn='" + this.rrn + "', txnDate=" + String.valueOf(this.txnDate) + ", txnAmount='" + this.txnAmount + "', holdDate=" + String.valueOf(this.holdDate) + ", holdRemark='" + this.holdRemark + "', network='" + this.network + "', totalAmt='" + this.totalAmt + "', status='" + this.status + "'}";
    }
}