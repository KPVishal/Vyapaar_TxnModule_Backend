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

    public String getVpa() {
        return vpa;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getMerchantAddress() {
        return merchantAddress;
    }

    public void setMerchantAddress(String merchantAddress) {
        this.merchantAddress = merchantAddress;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getLead_id() {
        return lead_id;
    }

    public void setLead_id(long lead_id) {
        this.lead_id = lead_id;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    @Override
    public String toString() {
        return "UserRequest{" + "mobileNo='" + mobileNo + '\'' + ", password='" + password + '\'' + ", ifscCode='" + ifscCode + '\'' + ", accountNo='" + accountNo + '\'' + ", vpa='" + vpa + '\'' + ", bankName='" + bankName + '\'' + ", merchantName='" + merchantName + '\'' + ", emailId='" + emailId + '\'' + ", branch='" + branch + '\'' + ", merchantAddress='" + merchantAddress + '\'' + ", lead_id=" + lead_id + ", tid='" + tid + '\'' + '}';
    }
}
