package com.bijlipay.model.dto;

public class UserRequest {
    private String mobileNo;
    private String password;
    private String ifscCode;
    private String accountNo;
    private String vpa;
    private String bankName;
    private String merchantName;
    private String emailId;
    private String branch;
    private String merchantAddress;
    private long lead_id;
    private String tid;

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

    public String getVpa() {
        return this.vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getBankName() {
        return this.bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getMerchantName() {
        return this.merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getEmailId() {
        return this.emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getBranch() {
        return this.branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getMerchantAddress() {
        return this.merchantAddress;
    }

    public void setMerchantAddress(String merchantAddress) {
        this.merchantAddress = merchantAddress;
    }

    public String getMobileNo() {
        return this.mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getLead_id() {
        return this.lead_id;
    }

    public void setLead_id(long lead_id) {
        this.lead_id = lead_id;
    }

    public String getTid() {
        return this.tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String toString() {
        return "UserRequest{mobileNo='" + this.mobileNo + "', password='" + this.password + "', ifscCode='" + this.ifscCode + "', accountNo='" + this.accountNo + "', vpa='" + this.vpa + "', bankName='" + this.bankName + "', merchantName='" + this.merchantName + "', emailId='" + this.emailId + "', branch='" + this.branch + "', merchantAddress='" + this.merchantAddress + "', lead_id=" + this.lead_id + ", tid='" + this.tid + "'}";
    }
}