package com.bijlipay.model;

import com.bijlipay.model.dto.UserRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "users"
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY
    )
    public List<UserTidVpaData> userTidVpaData;
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "user_id",
            nullable = false
    )
    private long id;
    @Column(
            name = "email",
            nullable = true
    )
    private String email;
    @Column(
            name = "mobile_no",
            nullable = false
    )
    private @NotBlank @Size(
            max = 10
    ) String mobileNo;
    @JsonIgnore
    @Column(
            name = "is_active"
    )
    private boolean isActive = true;
    @Column(
            name = "device_token",
            nullable = true
    )
    private String deviceToken;
    @JsonIgnore
    @Column(
            name = "password",
            nullable = false
    )
    private @NotBlank @Size(
            max = 100
    ) String password;
    @Column(
            name = "merchant_name",
            nullable = true
    )
    private String merchantName;
    @Column(
            name = "merchant_address",
            nullable = true
    )
    private String merchantAddress;
    @Column(
            name = "bank_name",
            nullable = true
    )
    private String bankName;
    @Column(
            name = "ifsc_code",
            nullable = true
    )
    private String ifscCode;
    @Column(
            name = "account_no",
            nullable = true
    )
    private String accountNo;
    @Column(
            name = "branch",
            nullable = true
    )
    private String branch;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(
            name = "created_at"
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd"
    )
    private Date created_at;
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(
            name = "updated_at"
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd"
    )
    private Date updated_at;
    @Column(
            name = "profile_pic_name"
    )
    private String profilePicName;
    @Column(
            name = "profile_pic_type"
    )
    private String profilePicType;
    @Column(
            name = "profile_pic_original_name",
            length = 1000
    )
    private String profilePicOriginalName;
    @Column(
            name = "is_account_non_expired",
            nullable = false
    )
    private boolean accountNonExpired;
    @Column(
            name = "is_account_non_locked",
            nullable = false
    )
    private boolean accountNonLocked;
    @Column(
            name = "is_credentials_non_expired",
            nullable = false
    )
    private boolean credentialsNonExpired;
    @Column(
            name = "is_speak_out_enable",
            nullable = true
    )
    private boolean isSpeakOutEnable;
    @Column(
            name = "language",
            nullable = true
    )
    private String language;
    @Column(
            name = "lead_id",
            nullable = true
    )
    private String leadId;
    @Column(
            name = "qr_lead_id",
            nullable = true
    )
    private String qrLeadId;
    @Column(
            name = "merchant_category",
            nullable = true
    )
    private int merchantCategory;

    public static User from(UserRequest userRequest) {
        User u = new User();
        u.setMobileNo(userRequest.getMobileNo());
        u.setPassword(userRequest.getPassword());
        return u;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNo() {
        return this.mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public String getProfilePicName() {
        return this.profilePicName;
    }

    public void setProfilePicName(String profilePicName) {
        this.profilePicName = profilePicName;
    }

    public String getProfilePicType() {
        return this.profilePicType;
    }

    public void setProfilePicType(String profilePicType) {
        this.profilePicType = profilePicType;
    }

    public String getProfilePicOriginalName() {
        return this.profilePicOriginalName;
    }

    public void setProfilePicOriginalName(String profilePicOriginalName) {
        this.profilePicOriginalName = profilePicOriginalName;
    }

    public String getDeviceToken() {
        return this.deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public Date getUpdated_at() {
        return this.updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public Date getCreated_at() {
        return this.created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getMerchantName() {
        return this.merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantAddress() {
        return this.merchantAddress;
    }

    public void setMerchantAddress(String merchantAddress) {
        this.merchantAddress = merchantAddress;
    }

    public String getBankName() {
        return this.bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getIfscCode() {
        return this.ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getAccountNo() {
        return this.accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getBranch() {
        return this.branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public boolean isSpeakOutEnable() {
        return this.isSpeakOutEnable;
    }

    public void setSpeakOutEnable(boolean isSpeakOutEnable) {
        this.isSpeakOutEnable = isSpeakOutEnable;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLeadId() {
        return this.leadId;
    }

    public void setLeadId(String leadId) {
        this.leadId = leadId;
    }

    public String getQrLeadId() {
        return this.qrLeadId;
    }

    public void setQrLeadId(String qrLeadId) {
        this.qrLeadId = qrLeadId;
    }

    public int getMerchantCategory() {
        return this.merchantCategory;
    }

    public void setMerchantCategory(int merchantCategory) {
        this.merchantCategory = merchantCategory;
    }

    public List<UserTidVpaData> getUserTidVpaData() {
        return this.userTidVpaData;
    }

    public void setUserTidVpaData(List<UserTidVpaData> userTidVpaData) {
        this.userTidVpaData = userTidVpaData;
    }
}