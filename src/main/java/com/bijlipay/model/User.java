package com.bijlipay.model;

import com.bijlipay.model.dto.UserRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User{
        // FIX: Replaced @JsonManagedReference with @JsonIgnore.
        // @JsonManagedReference was instructing Jackson to serialize this lazy
        // collection, but the Hibernate session is already closed by the time
        // Jackson runs — causing LazyInitializationException wrapped as
        // HttpMessageNotWritableException. @JsonIgnore prevents Jackson from
        // ever touching this collection; data is fetched explicitly via
        // userTidVpaDataService.getByUserId() when needed.
        @JsonIgnore
        @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
        public List<UserTidVpaData> userTidVpaData;
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "user_id", nullable = false)
        private long id;
        @Column(name = "email", nullable = true)
        private String email;
        @NotBlank
        @Size(max = 10)
        @Column(name = "mobile_no", nullable = false)
        private String mobileNo;
        @JsonIgnore
        @Column(name = "is_active")
        private boolean isActive = true;
        @Column(name = "device_token", nullable = true)
        private String deviceToken;
        @JsonIgnore
        @NotBlank
        @Size(max = 100)
        @Column(name = "password", nullable = false)
        private String password;
        @Column(name = "merchant_name", nullable = true)
        private String merchantName;
        @Column(name = "merchant_address", nullable = true)
        private String merchantAddress;
        @Column(name = "bank_name", nullable = true)
        private String bankName;
        @Column(name = "ifsc_code", nullable = true)
        private String ifscCode;
        @Column(name = "account_no", nullable = true)
        private String accountNo;
        @Column(name = "branch", nullable = true)
        private String branch;
        @CreationTimestamp
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "created_at")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date created_at;
        @UpdateTimestamp
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "updated_at")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date updated_at;
        @Column(name = "profile_pic_name")
        private String profilePicName;
        @Column(name = "profile_pic_type")
        private String profilePicType;
        @Column(name = "profile_pic_original_name", length = 1000)
        private String profilePicOriginalName;
        @Column(name = "is_account_non_expired", nullable = false)
        private boolean accountNonExpired;
        @Column(name = "is_account_non_locked", nullable = false)
        private boolean accountNonLocked;
        @Column(name = "is_credentials_non_expired", nullable = false)
        private boolean credentialsNonExpired;
        @Column(name = "is_speak_out_enable", nullable = true)
        private boolean isSpeakOutEnable;
        @Column(name = "language", nullable = true)
        private String language;
        @Column(name = "lead_id", nullable = true)
        private String leadId;
        @Column(name = "qr_lead_id", nullable = true)
        private String qrLeadId;
        @Column(name = "merchant_category", nullable = true)
        private int merchantCategory;   // 1- pos merchant 2- pos with qr merchant 3-qr merchant


        public User() {
            super();
        }

        public static User from(UserRequest userRequest) {
            User u = new User();
            u.setMobileNo(userRequest.getMobileNo());
            u.setPassword(userRequest.getPassword());
            return u;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

//    public List<MerchantDetails> getMerchantDeatils() {
//        return merchantDeatils;
//    }
//
//    public void setMerchantDeatils(List<MerchantDetails> merchantDeatils) {
//        this.merchantDeatils = merchantDeatils;
//    }

        public String getMobileNo() {
            return mobileNo;
        }

        public void setMobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        public boolean isAccountNonExpired() {
            return accountNonExpired;
        }

        public void setAccountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
        }

        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }

        public void setAccountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
        }

        public boolean isCredentialsNonExpired() {
            return credentialsNonExpired;
        }

        public void setCredentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
        }

        public String getProfilePicName() {
            return profilePicName;
        }

        public void setProfilePicName(String profilePicName) {
            this.profilePicName = profilePicName;
        }

        public String getProfilePicType() {
            return profilePicType;
        }

        public void setProfilePicType(String profilePicType) {
            this.profilePicType = profilePicType;
        }

        public String getProfilePicOriginalName() {
            return profilePicOriginalName;
        }

        public void setProfilePicOriginalName(String profilePicOriginalName) {
            this.profilePicOriginalName = profilePicOriginalName;
        }

        public String getDeviceToken() {
            return deviceToken;
        }

        public void setDeviceToken(String deviceToken) {
            this.deviceToken = deviceToken;
        }

        public Date getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(Date updated_at) {
            this.updated_at = updated_at;
        }

        public Date getCreated_at() {
            return created_at;
        }

        public void setCreated_at(Date created_at) {
            this.created_at = created_at;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean isActive) {
            this.isActive = isActive;
        }

        public String getMerchantName() {
            return merchantName;
        }

        public void setMerchantName(String merchantName) {
            this.merchantName = merchantName;
        }

        public String getMerchantAddress() {
            return merchantAddress;
        }

        public void setMerchantAddress(String merchantAddress) {
            this.merchantAddress = merchantAddress;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getIfscCode() {
            return ifscCode;
        }

        public void setIfscCode(String ifscCode) {
            this.ifscCode = ifscCode;
        }

        public String getAccountNo() {
            return accountNo;
        }

        public void setAccountNo(String accountNo) {
            this.accountNo = accountNo;
        }

        public String getBranch() {
            return branch;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }

        public boolean isSpeakOutEnable() {
            return isSpeakOutEnable;
        }

        public void setSpeakOutEnable(boolean isSpeakOutEnable) {
            this.isSpeakOutEnable = isSpeakOutEnable;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getLeadId() {
            return leadId;
        }

        public void setLeadId(String leadId) {
            this.leadId = leadId;
        }

        public String getQrLeadId() {
            return qrLeadId;
        }

        public void setQrLeadId(String qrLeadId) {
            this.qrLeadId = qrLeadId;
        }

        public int getMerchantCategory() {
            return merchantCategory;
        }

        public void setMerchantCategory(int merchantCategory) {
            this.merchantCategory = merchantCategory;
        }

//    public List<QrVpaDetails> getQrVpaDetails() {
//        return qrVpaDetails;
//    }
//
//    public void setQrVpaDetails(List<QrVpaDetails> qrVpaDetails) {
//        this.qrVpaDetails = qrVpaDetails;
//    }


        public List<UserTidVpaData> getUserTidVpaData() {
            return userTidVpaData;
        }

        public void setUserTidVpaData(List<UserTidVpaData> userTidVpaData) {
            this.userTidVpaData = userTidVpaData;
        }
}
