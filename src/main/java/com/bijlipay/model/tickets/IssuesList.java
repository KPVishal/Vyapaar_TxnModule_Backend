package com.bijlipay.model.tickets;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import org.hibernate.annotations.Where;

@Entity
@Table(
        name = "issues_list"
)
@Where(
        clause = "is_active=true"
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IssuesList {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "issue_id",
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
    @JsonIgnore
    @Column(
            name = "is_active"
    )
    private boolean isActive = true;
    @Column(
            name = "issue_type"
    )
    private int issueType;
    @Column(
            name = "description"
    )
    private String description;

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

    public int getIssueType() {
        return this.issueType;
    }

    public void setIssueType(int issueType) {
        this.issueType = issueType;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        long var10000 = this.id;
        return "IssuesList{id=" + var10000 + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ", isActive=" + this.isActive + ", issueType=" + this.issueType + ", description='" + this.description + "'}";
    }
}