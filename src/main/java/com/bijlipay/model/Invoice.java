package com.bijlipay.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(
        name = "invoice"
)
public class Invoice {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "invoice_id"
    )
    private int invoiceId;
    private transient int type;
    private String tid;
    private String email;
    private String invoiceDate;
    private String invoiceType;

    public int getInvoiceId() {
        return this.invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceType() {
        return this.invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTid() {
        return this.tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInvoiceDate() {
        return this.invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String toString() {
        return "Invoice{invoiceId=" + this.invoiceId + ", type=" + this.type + ", tid='" + this.tid + "', email='" + this.email + "', invoiceDate='" + this.invoiceDate + "', invoiceType='" + this.invoiceType + "'}";
    }
}