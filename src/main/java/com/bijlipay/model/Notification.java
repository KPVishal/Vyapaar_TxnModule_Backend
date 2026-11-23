package com.bijlipay.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;
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

@Entity
@Table(
        name = "notifications"
)
public class Notification implements Serializable {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "notification_id"
    )
    protected Long id;
    @Column(
            name = "notification_title",
            nullable = true
    )
    protected String notificationTitle;
    @Column(
            name = "notification_reason",
            nullable = true
    )
    protected String notificationReason;
    @Column(
            name = "notification_redirectTo",
            nullable = true
    )
    protected String notificationRedirectTo;
    @Column(
            name = "notification_seen"
    )
    protected boolean notificationSeen = false;
    @JsonBackReference
    @ManyToOne(
            optional = true
    )
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "user_id"
    )
    protected User user;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(
            name = "notification_date"
    )
    protected Date notificationDate;
    @Column(
            name = "notification_data",
            columnDefinition = "TEXT"
    )
    protected String data;

    public Notification() {
    }

    public Notification(Long notif_id, String notif_title, String notif_reason, String notif_redirectTo, boolean notif_seen, Date notif_date) {
        this.id = notif_id;
        this.notificationTitle = notif_title;
        this.notificationReason = notif_reason;
        this.notificationRedirectTo = notif_redirectTo;
        this.notificationSeen = notif_seen;
        this.notificationDate = notif_date;
    }

    public boolean isNotificationSeen() {
        return this.notificationSeen;
    }

    public void setNotificationSeen(boolean notificationSeen) {
        this.notificationSeen = notificationSeen;
    }

    public Date getNotificationDate() {
        return this.notificationDate;
    }

    public void setNotificationDate(Date notificationDate) {
        this.notificationDate = notificationDate;
    }

    public String getNotificationTitle() {
        return this.notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationReason() {
        return this.notificationReason;
    }

    public void setNotificationReason(String notificationReason) {
        this.notificationReason = notificationReason;
    }

    public String getNotificationRedirectTo() {
        return this.notificationRedirectTo;
    }

    public void setNotificationRedirectTo(String notificationRedirectTo) {
        this.notificationRedirectTo = notificationRedirectTo;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String notificationData) {
        this.data = notificationData;
    }

    public String toString() {
        Long var10000 = this.id;
        return "Notification{id=" + var10000 + ", notificationTitle='" + this.notificationTitle + "', notificationReason='" + this.notificationReason + "', notificationRedirectTo='" + this.notificationRedirectTo + "', notificationSeen=" + this.notificationSeen + ", user=" + String.valueOf(this.user) + ", notificationDate=" + String.valueOf(this.notificationDate) + ", data='" + this.data + "'}";
    }
}