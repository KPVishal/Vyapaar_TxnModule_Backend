package com.bijlipay.model.tickets;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tickets")
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tickets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id", nullable = false)
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
    @SerializedName("ticketNumber")
    @Column(name = "ticket_number")
    private String ticketNumber;
    @Column(name = "ticket_due_date")
    private Date ticketDueDate;
    @SerializedName("ticketTitle")
    @Column(name = "ticket_title")
    private String ticketTitle;
    @SerializedName("ticketDetails")
    @Column(name = "ticket_details")
    private String ticketDetails;
    @SerializedName("mid")
    @Column(name = "mid")
    private String mid;
    @SerializedName("tid")
    @Column(name = "tid")
    private String tid;
    @Column(name = "attachments")
    private String attachments;
    @Column(name = "status")
    private String status;
    @Column(name = "justification")
    private String justiFication;

    @SerializedName("vpa")
    @Column(name = "vpa")
    private String vpa;
    @Column(name = "federal_flag")
    private boolean federalFlag;

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

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Date getTicketDueDate() {
        return ticketDueDate;
    }

    public void setTicketDueDate(Date ticketDueDate) {
        this.ticketDueDate = ticketDueDate;
    }

    public String getTicketTitle() {
        return ticketTitle;
    }

    public void setTicketTitle(String ticketTitle) {
        this.ticketTitle = ticketTitle;
    }

    public String getTicketDetails() {
        return ticketDetails;
    }

    public void setTicketDetails(String ticketDetails) {
        this.ticketDetails = ticketDetails;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJustiFication() {
        return justiFication;
    }

    public void setJustiFication(String justiFication) {
        this.justiFication = justiFication;
    }

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public boolean isFederalFlag() {
        return federalFlag;
    }

    public void setFederalFlag(boolean federalFlag) {
        this.federalFlag = federalFlag;
    }

    @Override
    public String toString() {
        return "Tickets{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", ticketDueDate=" + ticketDueDate +
                ", ticketTitle='" + ticketTitle + '\'' +
                ", ticketDetails='" + ticketDetails + '\'' +
                ", mid='" + mid + '\'' +
                ", tid='" + tid + '\'' +
                ", attachments='" + attachments + '\'' +
                ", status='" + status + '\'' +
                ", justiFication='" + justiFication + '\'' +
                ", vpa='" + vpa + '\'' +
                ", federalFlag=" + federalFlag +
                '}';
    }
}
