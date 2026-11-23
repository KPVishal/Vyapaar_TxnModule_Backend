package com.bijlipay.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "settlement_request_response_data"
)
public class SettlementRequestResponse {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id"
    )
    private long id;
    @Column(
            name = "tid"
    )
    private String tid;
    @Column(
            name = "mid"
    )
    private String mid;
    @Column(
            name = "vpa"
    )
    private String vpa;
    @Column(
            name = "unique_id"
    )
    private String uniqueId;
    @Column(
            name = "from_date"
    )
    private String fromDate;
    @Column(
            name = "to_date"
    )
    private String toDate;
    @Column(
            name = "available_date"
    )
    private Date availableDate;
    @Column(
            name = "expiry_date"
    )
    private Date expiryDate;
    @Column(
            name = "file_url",
            columnDefinition = "TEXT"
    )
    private String fileUrl;
    @Column(
            name = "settlement_s3_url",
            columnDefinition = "TEXT"
    )
    private String settlementS3Url;
    @Column(
            name = "is_file_exist",
            columnDefinition = "tinyint(1) default 0"
    )
    private boolean isFileExist;
    @ManyToOne(
            optional = true
    )
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "user_id"
    )
    private User user;
    @Column(
            name = "category_type"
    )
    private String categoryType;
    @Column(
            name = "file_name"
    )
    private String fileName;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(
            name = "created_at"
    )
    private Date createdAt;
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(
            name = "updated_at"
    )
    private Date updatedAt;
    @Column(
            name = "mime_type"
    )
    private String fileType;

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

    public String getVpa() {
        return this.vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getFromDate() {
        return this.fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return this.toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public Date getAvailableDate() {
        return this.availableDate;
    }

    public void setAvailableDate(Date availableDate) {
        this.availableDate = availableDate;
    }

    public Date getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getFileUrl() {
        return this.fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getSettlementS3Url() {
        return this.settlementS3Url;
    }

    public void setSettlementS3Url(String settlementS3Url) {
        this.settlementS3Url = settlementS3Url;
    }

    public boolean isFileExist() {
        return this.isFileExist;
    }

    public void setFileExist(boolean fileExist) {
        this.isFileExist = fileExist;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getCategoryType() {
        return this.categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return this.fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}