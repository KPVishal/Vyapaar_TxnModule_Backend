package com.bijlipay.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(
        name = "user_tid_vpa_data"
)
public class UserTidVpaData {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id"
    )
    private long id;
    @Column(
            name = "tid",
            nullable = true
    )
    private String tid;
    @Column(
            name = "mid",
            nullable = true
    )
    private String mid;
    @JsonIgnore
    @Column(
            name = "is_active"
    )
    private boolean isActive = true;
    @Column(
            name = "is_bijlipay_switch"
    )
    private boolean isBijlipaySwitch = true;
    @Column(
            name = "lead_id",
            nullable = true
    )
    private long leadId;
    @Column(
            name = "created_at",
            nullable = true
    )
    private String createdAt;
    @Column(
            name = "is_scanned",
            nullable = true
    )
    private String isScanned;
    @Column(
            name = "updated_at",
            nullable = true
    )
    private String updatedAt;
    @Column(
            name = "vpa",
            nullable = true
    )
    private String vpa;
    @Column(
            name = "qr_lead_id",
            nullable = true
    )
    private String qrLeadId;
    @JsonBackReference
    @ManyToOne(
            optional = false,
            fetch = FetchType.EAGER
    )
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "user_id"
    )
    private User user;
    @Column(
            name = "is_qr",
            columnDefinition = "tinyint(1) default 0"
    )
    private boolean isQR;
    @Column(
            name = "institution_code"
    )
    private String institutionCode;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTid() {
        return this.tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getMid() {
        return this.mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isBijlipaySwitch() {
        return this.isBijlipaySwitch;
    }

    public void setBijlipaySwitch(boolean bijlipaySwitch) {
        this.isBijlipaySwitch = bijlipaySwitch;
    }

    public long getLeadId() {
        return this.leadId;
    }

    public void setLeadId(long leadId) {
        this.leadId = leadId;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getIsScanned() {
        return this.isScanned;
    }

    public void setIsScanned(String isScanned) {
        this.isScanned = isScanned;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getVpa() {
        return this.vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getQrLeadId() {
        return this.qrLeadId;
    }

    public void setQrLeadId(String qrLeadId) {
        this.qrLeadId = qrLeadId;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isQR() {
        return this.isQR;
    }

    public void setQR(boolean QR) {
        this.isQR = QR;
    }

    public String getInstitutionCode() {
        return this.institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }
}