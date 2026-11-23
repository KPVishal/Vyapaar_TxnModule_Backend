package com.bijlipay.model.tickets;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.annotations.SerializedName;
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

@Entity
@Table( name = "tickets")
public class Tickets {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    @Column( name = "ticket_id", nullable = false)
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
    @SerializedName("ticketNumber")
    @Column(
            name = "ticket_number"
    )
    private String ticketNumber;
    @Column(
            name = "ticket_due_date"
    )
    private Date ticketDueDate;
    @SerializedName("ticketTitle")
    @Column(
            name = "ticket_title"
    )
    private String ticketTitle;
    @SerializedName("ticketDetails")
    @Column(
            name = "ticket_details"
    )
    private String ticketDetails;
    @SerializedName("mid")
    @Column(
            name = "mid"
    )
    private String mid;
    @SerializedName("tid")
    @Column(
            name = "tid"
    )
    private String tid;
    @Column(
            name = "attachments"
    )
    private String attachments;
    @Column(
            name = "status"
    )
    private String status;
    @Column(
            name = "justification"
    )
    private String justiFication;
    @SerializedName("vpa")
    @Column(
            name = "vpa"
    )
    private String vpa;
    @Column(
            name = "federal_flag"
    )
    private boolean federalFlag;

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

    public String getTicketNumber() {
        return this.ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Date getTicketDueDate() {
        return this.ticketDueDate;
    }

    public void setTicketDueDate(Date ticketDueDate) {
        this.ticketDueDate = ticketDueDate;
    }

    public String getTicketTitle() {
        return this.ticketTitle;
    }

    public void setTicketTitle(String ticketTitle) {
        this.ticketTitle = ticketTitle;
    }

    public String getTicketDetails() {
        return this.ticketDetails;
    }

    public void setTicketDetails(String ticketDetails) {
        this.ticketDetails = ticketDetails;
    }

    public String getMid() {
        return this.mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTid() {
        return this.tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getAttachments() {
        return this.attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJustiFication() {
        return this.justiFication;
    }

    public void setJustiFication(String justiFication) {
        this.justiFication = justiFication;
    }

    public String getVpa() {
        return this.vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public boolean isFederalFlag() {
        return this.federalFlag;
    }

    public void setFederalFlag(boolean federalFlag) {
        this.federalFlag = federalFlag;
    }

    public String toString() {
        long var10000 = this.id;
        return "Tickets{id=" + var10000 + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ", ticketNumber='" + this.ticketNumber + "', ticketDueDate=" + String.valueOf(this.ticketDueDate) + ", ticketTitle='" + this.ticketTitle + "', ticketDetails='" + this.ticketDetails + "', mid='" + this.mid + "', tid='" + this.tid + "', attachments='" + this.attachments + "', status='" + this.status + "', justiFication='" + this.justiFication + "', vpa='" + this.vpa + "', federalFlag=" + this.federalFlag + "}";
    }
}