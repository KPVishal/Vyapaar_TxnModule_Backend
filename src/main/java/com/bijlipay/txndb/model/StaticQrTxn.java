package com.bijlipay.txndb.model;

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

@Entity
@Table(
        name = "static_qr_txn"
)
public class StaticQrTxn {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "txn_id",
            nullable = false
    )
    private long id;
    @Column(
            name = "customer_vpa"
    )
    private String customerVpa;
    @Column(
            name = "credit_vpa"
    )
    private String creditVpa;
    @Column(
            name = "merchant_id"
    )
    private String merchantId;
    @Column(
            name = "merchant_channel_id"
    )
    private String merchantChannelId;
    @Column(
            name = "merchant_transaction_id"
    )
    private String merchantTransactionId;
    @Column(
            name = "transaction_timestamp"
    )
    private String transactionTimestamp;
    @Column(
            name = "transaction_amount"
    )
    private float transactionAmount;
    @Column(
            name = "gateway_transaction_id"
    )
    private String gatewayTransactionId;
    @Column(
            name = "gateway_response_code"
    )
    private String gatewayResponseCode;
    @Column(
            name = "gateway_response_message"
    )
    private String gatewayResponseMessage;
    @Column(
            name = "rrn"
    )
    private String rrn;
    @Column(
            name = "checksum"
    )
    private String checksum;
    @Column(
            name = "payeractype"
    )
    private String payeractype;
    @Column(
            name = "purpose_code"
    )
    private String purposeCode;
    @Column(
            name = "init_mode"
    )
    private String initMode;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(
            name = "created_at"
    )
    private Date createdAt;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public float getTransactionAmount() {
        return this.transactionAmount;
    }

    public void setTransactionAmount(float transactionAmount) {
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

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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
}