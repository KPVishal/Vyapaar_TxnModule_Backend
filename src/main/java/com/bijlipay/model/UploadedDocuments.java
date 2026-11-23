package com.bijlipay.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(
        name = "uploaded_documents"
)
@Where(
        clause = "is_active!=0"
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UploadedDocuments {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "uploaded_document_id",
            nullable = false
    )
    private long id;
    @Column(
            name = "file_name",
            nullable = false
    )
    private @NotBlank String fileName;
    @Column(
            name = "is_active"
    )
    private boolean isActive;
    @Column(
            name = "file_name_original",
            nullable = true
    )
    private String fileNameOriginal;
    @Column(
            name = "mime_type",
            nullable = true
    )
    private String mimeType;
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
    @JsonBackReference
    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "category_id",
            referencedColumnName = "category_id"
    )
    private Category category;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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

    public Category getCategory() {
        return this.category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getFileNameOriginal() {
        return this.fileNameOriginal;
    }

    public void setFileNameOriginal(String fileNameOriginal) {
        this.fileNameOriginal = fileNameOriginal;
    }

    public String toString() {
        long var10000 = this.id;
        return "UploadedDocuments [id=" + var10000 + ", fileName=" + this.fileName + ", fileNameOriginal=" + this.fileNameOriginal + ", mimeType=" + this.mimeType + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + "]";
    }
}