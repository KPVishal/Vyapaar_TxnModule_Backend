package com.bijlipay.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import org.hibernate.annotations.Where;

@Entity
@Table(
        name = "category"
)
@Where(
        clause = "is_active=true"
)
public class Category {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "category_id",
            nullable = false
    )
    private long id;
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
    @JsonBackReference
    @ManyToOne(
            optional = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "user_id"
    )
    private User user;
    @JsonIgnore
    @Column(
            name = "is_active",
            columnDefinition = "tinyint(1) default 0"
    )
    private boolean isActive = true;
    @Column(
            name = "category_type"
    )
    private int categoryType;
    @Column(
            name = "description"
    )
    private String description;
    @Column(
            name = "settlement_file_url",
            columnDefinition = "TEXT"
    )
    private String settlementFileUrl;
    @Column(
            name = "file_url",
            columnDefinition = "TEXT"
    )
    private String fileUrl;
    @JsonBackReference
    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "merchant_id",
            referencedColumnName = "id"
    )
    private UserTidVpaData userTidVpaData;

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
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

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public int getCategoryType() {
        return this.categoryType;
    }

    public void setCategoryType(int categoryType) {
        this.categoryType = categoryType;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserTidVpaData getUserTidVpaData() {
        return this.userTidVpaData;
    }

    public void setUserTidVpaData(UserTidVpaData userTidVpaData) {
        this.userTidVpaData = userTidVpaData;
    }

    public String getSettlementFileUrl() {
        return this.settlementFileUrl;
    }

    public void setSettlementFileUrl(String settlementFileUrl) {
        this.settlementFileUrl = settlementFileUrl;
    }

    public String getFileUrl() {
        return this.fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String toString() {
        long var10000 = this.id;
        return "Category{id=" + var10000 + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ", user=" + String.valueOf(this.user) + ", isActive=" + this.isActive + ", categoryType=" + this.categoryType + ", description='" + this.description + "', settlementFileUrl='" + this.settlementFileUrl + "', fileUrl='" + this.fileUrl + "', userTidVpaData=" + String.valueOf(this.userTidVpaData) + "}";
    }
}