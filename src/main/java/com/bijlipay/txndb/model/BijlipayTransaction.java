package com.bijlipay.txndb.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "bijlipayTransaction",
        indexes = {@Index(
                name = "idx_tid_resp_code_resp_time",
                columnList = "terminal_id, txn_response_code, response_received_time DESC"
        ), @Index(
                name = "idx_tid_resp_time",
                columnList = "terminal_id, response_received_time DESC"
        ), @Index(
                name = "idx_mti_tid_stan",
                columnList = "mti, terminal_id, stan"
        ), @Index(
                name = "idx_tid_rrn_mti",
                columnList = "terminal_id, rrn_number, mti"
        ), @Index(
                name = "idx_tid_mti_resp_code_type",
                columnList = "terminal_id, mti, txn_response_code, txn_type"
        )}
)
public class BijlipayTransaction {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "transaction_id",
            nullable = false
    )
    private long id;
    @Column(
            name = "mti",
            nullable = true
    )
    private String mti;
    @Column(
            name = "txn_type",
            nullable = true
    )
    private String txnType;
    @Column(
            name = "terminal_id",
            nullable = true
    )
    private String terminalId;
    @Column(
            name = "merchant_id",
            nullable = true
    )
    private String merchantId;
    @Column(
            name = "txn_date",
            nullable = true
    )
    private String txnDate;
    @Column(
            name = "txn_time",
            nullable = true
    )
    private String txnTime;
    @Column(
            name = "txn_amount",
            nullable = true
    )
    private Double txnAmount;
    @Column(
            name = "txn_response_code",
            nullable = true
    )
    private String txnResponseCode;
    @Column(
            name = "response_received_time",
            nullable = true
    )
    private Date responseReceivedTime;
    @Column(
            name = "rrn_number",
            nullable = true
    )
    private String rRNumber;
    @Column(
            name = "stan",
            nullable = true
    )
    private String stan;
    @Column(
            name = "invoice_number",
            nullable = true
    )
    private String invoiceNumber;
    @Column(
            name = "batch_number",
            nullable = true
    )
    private String batchNumber;
    @Column(
            name = "txn_additional_amount",
            nullable = true
    )
    private Double TxnAdditionalAmount;
    @Column(
            name = "urn",
            nullable = true
    )
    private String urn;
    @Column(
            name = "masked_card_number",
            nullable = true
    )
    private String maskedcardnumber;
    @Column(
            name = "txn_correlation_id",
            nullable = true
    )
    private String txnCorrelationId;
    @Column(
            name = "auth_code",
            nullable = true
    )
    private String authCode;
    @Column(
            name = "panentry_mode",
            nullable = true
    )
    private String panEntryMode;
    @Column(
            name = "bin",
            nullable = true
    )
    private String bin;
    @Column(
            name = "customer_vpa",
            nullable = true
    )
    private String customerVpa;
    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = true
    )
    private Date updatedAt;

    public String getMaskedcardnumber() {
        return this.maskedcardnumber;
    }

    public void setMaskedcardnumber(String maskedcardnumber) {
        this.maskedcardnumber = maskedcardnumber;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMti() {
        return this.mti;
    }

    public void setMti(Object mti) {
        this.mti = mti == null ? "NA" : mti.toString();
    }

    public String getTxnType() {
        return this.txnType;
    }

    public void setTxnType(Object txnType) {
        this.txnType = txnType == null ? "NA" : txnType.toString();
    }

    public String getTerminalId() {
        return this.terminalId;
    }

    public void setTerminalId(Object terminalId) {
        this.terminalId = terminalId == null ? "NA" : terminalId.toString();
    }

    public String getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(Object merchantId) {
        this.merchantId = merchantId == null ? "NA" : merchantId.toString();
    }

    public String getTxnDate() {
        return this.txnDate;
    }

    public void setTxnDate(Object txnDate) {
        this.txnDate = txnDate == null ? "NA" : txnDate.toString();
    }

    public String getTxnTime() {
        return this.txnTime;
    }

    public void setTxnTime(Object txnTime) {
        this.txnTime = txnTime == null ? "NA" : txnTime.toString();
    }

    public Double getTxnAmount() {
        return this.txnAmount;
    }

    public void setTxnAmount(Object txnAmount) {
        this.txnAmount = txnAmount != null && txnAmount.toString() != "" ? Double.parseDouble(txnAmount.toString()) : (double)0.0F;
    }

    public String getTxnResponseCode() {
        return this.txnResponseCode;
    }

    public void setTxnResponseCode(Object txnResponseCode) {
        this.txnResponseCode = txnResponseCode == null ? "NA" : txnResponseCode.toString();
    }

    public Date getResponseReceivedTime() {
        return this.responseReceivedTime;
    }

    public void setResponseReceivedTime(Date responseReceivedTime) {
        this.responseReceivedTime = responseReceivedTime;
    }

    public String getrRNumber() {
        return this.rRNumber;
    }

    public void setrRNumber(Object rRNumber) {
        this.rRNumber = rRNumber == null ? "NA" : rRNumber.toString();
    }

    public String getStan() {
        return this.stan;
    }

    public void setStan(Object stan) {
        this.stan = stan == null ? "NA" : stan.toString();
    }

    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    public void setInvoiceNumber(Object invoiceNumber) {
        this.invoiceNumber = invoiceNumber == null ? "NA" : invoiceNumber.toString();
    }

    public String getBatchNumber() {
        return this.batchNumber;
    }

    public void setBatchNumber(Object batchNumber) {
        this.batchNumber = batchNumber == null ? "NA" : batchNumber.toString();
    }

    public Double getTxnAdditionalAmount() {
        return this.TxnAdditionalAmount;
    }

    public void setTxnAdditionalAmount(Object txnAdditionalAmount) {
        this.TxnAdditionalAmount = txnAdditionalAmount != null && txnAdditionalAmount.toString() != "" ? Double.parseDouble(txnAdditionalAmount.toString()) : (double)0.0F;
    }

    public String getUrn() {
        return this.urn;
    }

    public void setUrn(Object urn) {
        this.urn = urn == null ? "NA" : urn.toString();
    }

    public String getTxnCorrelationId() {
        return this.txnCorrelationId;
    }

    public void setTxnCorrelationId(Object txnCorrelationId) {
        this.txnCorrelationId = txnCorrelationId == null ? "NA" : txnCorrelationId.toString();
    }

    public String getAuthCode() {
        return this.authCode;
    }

    public void setAuthCode(Object authCode) {
        this.authCode = authCode == null ? "NA" : authCode.toString();
    }

    public String getPanEntryMode() {
        return this.panEntryMode;
    }

    public void setPanEntryMode(String panEntryMode) {
        this.panEntryMode = panEntryMode;
    }

    public String getBin() {
        return this.bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getCustomerVpa() {
        return this.customerVpa;
    }

    public void setCustomerVpa(String customerVpa) {
        this.customerVpa = customerVpa;
    }

    public Date getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String toString() {
        long var10000 = this.id;
        return "BijlipayTransaction{id=" + var10000 + ", mti='" + this.mti + "', txnType='" + this.txnType + "', terminalId='" + this.terminalId + "', merchantId='" + this.merchantId + "', txnDate='" + this.txnDate + "', txnTime='" + this.txnTime + "', txnAmount=" + this.txnAmount + ", txnResponseCode='" + this.txnResponseCode + "', responseReceivedTime=" + String.valueOf(this.responseReceivedTime) + ", rRNumber='" + this.rRNumber + "', stan='" + this.stan + "', invoiceNumber='" + this.invoiceNumber + "', batchNumber='" + this.batchNumber + "', TxnAdditionalAmount=" + this.TxnAdditionalAmount + ", urn='" + this.urn + "', maskedcardnumber='" + this.maskedcardnumber + "', txnCorrelationId='" + this.txnCorrelationId + "', authCode='" + this.authCode + "', panEntryMode='" + this.panEntryMode + "', bin='" + this.bin + "', customerVpa='" + this.customerVpa + "'}";
    }
}