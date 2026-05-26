package com.bijlipay.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "category")
@Where(clause = "is_active=true")
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private long id;
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
    @JsonBackReference()
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
    @JsonIgnore
    @Column(name = "is_active", columnDefinition = "tinyint(1) default 0")
    private boolean isActive = true;
    @Column(name = "category_type")
    private int categoryType; //1.risk hold 2.paperRoll 3.offers 4.settlement 5.Tickets 6.Transactions
    @Column(name = "description")
    private String description;
    @Column(name = "settlement_file_url", columnDefinition = "TEXT")
    private String settlementFileUrl;

    @Column(name = "file_url", columnDefinition = "TEXT")
    private String fileUrl;

    @JsonBackReference()
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", referencedColumnName = "id")
    private UserTidVpaData userTidVpaData;
//    @OneToMany(mappedBy = "category", orphanRemoval = true)
//    private List<UploadedDocuments> uploadedDocuments;
//
//    public List<UploadedDocuments> getUploadedDocuments() {
//        return uploadedDocuments;
//    }
//
//    public void setUploadedDocuments(List<UploadedDocuments> uploadedDocuments) {
//        this.uploadedDocuments = uploadedDocuments;
//    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(int categoryType) {
        this.categoryType = categoryType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserTidVpaData getUserTidVpaData() {
        return userTidVpaData;
    }

    public void setUserTidVpaData(UserTidVpaData userTidVpaData) {
        this.userTidVpaData = userTidVpaData;
    }

    public String getSettlementFileUrl() {
        return settlementFileUrl;
    }

    public void setSettlementFileUrl(String settlementFileUrl) {
        this.settlementFileUrl = settlementFileUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", user=" + user +
                ", isActive=" + isActive +
                ", categoryType=" + categoryType +
                ", description='" + description + '\'' +
                ", settlementFileUrl='" + settlementFileUrl + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", userTidVpaData=" + userTidVpaData +
                '}';
    }
}
