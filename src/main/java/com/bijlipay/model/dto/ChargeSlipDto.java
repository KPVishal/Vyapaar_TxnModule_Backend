package com.bijlipay.model.dto;

import java.util.Date;

public class ChargeSlipDto {
    private Long id;
    private String TID;
    private String MID;
    private String TXN_TYPE;
    private String TXN_DATE;
    private String TXN_TIME;
    private String BATCH_NUMBER;
    private String INVOICE_NUMBER;
    private String CARD_MASKED;
    private String TXN_AMOUNT_SALE;
    private String TXN_AMOUNT_TIP;
    private String TXN_AMOUNT_TOTAL;
    private String TVR;
    private String TSI;
    private String TXN_CERTIFICATE;
    private String BANK_LOGO_ID;
    private String RRN;
    private String MODE;
    private String CARD_HOLDER_NAME;
    private String APPLICATION_ID;
    private String APPLICATION_NAME;
    private String APPLICATION_VERSION;
    private String AUTH_CODE;
    private String PIN_VERIFICATION;
    private String CARD_TYPE;
    private String RECEIPT_HEADER1;
    private String MERCHANT_NAME;
    private String LOCATION;
    private String MOBILE_NUMBER;
    private String RECEIPT_FOOTER1;
    private Date createdDate;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTID() {
        return this.TID;
    }

    public void setTID(String TID) {
        this.TID = TID;
    }

    public String getMID() {
        return this.MID;
    }

    public void setMID(String MID) {
        this.MID = MID;
    }

    public String getTXN_TYPE() {
        return this.TXN_TYPE;
    }

    public void setTXN_TYPE(String TXN_TYPE) {
        this.TXN_TYPE = TXN_TYPE;
    }

    public String getTXN_DATE() {
        return this.TXN_DATE;
    }

    public void setTXN_DATE(String TXN_DATE) {
        this.TXN_DATE = TXN_DATE;
    }

    public String getTXN_TIME() {
        return this.TXN_TIME;
    }

    public void setTXN_TIME(String TXN_TIME) {
        this.TXN_TIME = TXN_TIME;
    }

    public String getBATCH_NUMBER() {
        return this.BATCH_NUMBER;
    }

    public void setBATCH_NUMBER(String BATCH_NUMBER) {
        this.BATCH_NUMBER = BATCH_NUMBER;
    }

    public String getINVOICE_NUMBER() {
        return this.INVOICE_NUMBER;
    }

    public void setINVOICE_NUMBER(String INVOICE_NUMBER) {
        this.INVOICE_NUMBER = INVOICE_NUMBER;
    }

    public String getCARD_MASKED() {
        return this.CARD_MASKED;
    }

    public void setCARD_MASKED(String CARD_MASKED) {
        this.CARD_MASKED = CARD_MASKED;
    }

    public String getTXN_AMOUNT_SALE() {
        return this.TXN_AMOUNT_SALE;
    }

    public void setTXN_AMOUNT_SALE(String TXN_AMOUNT_SALE) {
        this.TXN_AMOUNT_SALE = TXN_AMOUNT_SALE;
    }

    public String getTXN_AMOUNT_TIP() {
        return this.TXN_AMOUNT_TIP;
    }

    public void setTXN_AMOUNT_TIP(String TXN_AMOUNT_TIP) {
        this.TXN_AMOUNT_TIP = TXN_AMOUNT_TIP;
    }

    public String getTXN_AMOUNT_TOTAL() {
        return this.TXN_AMOUNT_TOTAL;
    }

    public void setTXN_AMOUNT_TOTAL(String TXN_AMOUNT_TOTAL) {
        this.TXN_AMOUNT_TOTAL = TXN_AMOUNT_TOTAL;
    }

    public String getTVR() {
        return this.TVR;
    }

    public void setTVR(String TVR) {
        this.TVR = TVR;
    }

    public String getTSI() {
        return this.TSI;
    }

    public void setTSI(String TSI) {
        this.TSI = TSI;
    }

    public String getTXN_CERTIFICATE() {
        return this.TXN_CERTIFICATE;
    }

    public void setTXN_CERTIFICATE(String TXN_CERTIFICATE) {
        this.TXN_CERTIFICATE = TXN_CERTIFICATE;
    }

    public String getBANK_LOGO_ID() {
        return this.BANK_LOGO_ID;
    }

    public void setBANK_LOGO_ID(String BANK_LOGO_ID) {
        this.BANK_LOGO_ID = BANK_LOGO_ID;
    }

    public String getRRN() {
        return this.RRN;
    }

    public void setRRN(String RRN) {
        this.RRN = RRN;
    }

    public String getMODE() {
        return this.MODE;
    }

    public void setMODE(String MODE) {
        this.MODE = MODE;
    }

    public String getCARD_HOLDER_NAME() {
        return this.CARD_HOLDER_NAME;
    }

    public void setCARD_HOLDER_NAME(String CARD_HOLDER_NAME) {
        this.CARD_HOLDER_NAME = CARD_HOLDER_NAME;
    }

    public String getAPPLICATION_ID() {
        return this.APPLICATION_ID;
    }

    public void setAPPLICATION_ID(String APPLICATION_ID) {
        this.APPLICATION_ID = APPLICATION_ID;
    }

    public String getAPPLICATION_NAME() {
        return this.APPLICATION_NAME;
    }

    public void setAPPLICATION_NAME(String APPLICATION_NAME) {
        this.APPLICATION_NAME = APPLICATION_NAME;
    }

    public String getAPPLICATION_VERSION() {
        return this.APPLICATION_VERSION;
    }

    public void setAPPLICATION_VERSION(String APPLICATION_VERSION) {
        this.APPLICATION_VERSION = APPLICATION_VERSION;
    }

    public String getAUTH_CODE() {
        return this.AUTH_CODE;
    }

    public void setAUTH_CODE(String AUTH_CODE) {
        this.AUTH_CODE = AUTH_CODE;
    }

    public String getPIN_VERIFICATION() {
        return this.PIN_VERIFICATION;
    }

    public void setPIN_VERIFICATION(String PIN_VERIFICATION) {
        this.PIN_VERIFICATION = PIN_VERIFICATION;
    }

    public String getCARD_TYPE() {
        return this.CARD_TYPE;
    }

    public void setCARD_TYPE(String CARD_TYPE) {
        this.CARD_TYPE = CARD_TYPE;
    }

    public String getRECEIPT_HEADER1() {
        return this.RECEIPT_HEADER1;
    }

    public void setRECEIPT_HEADER1(String RECEIPT_HEADER1) {
        this.RECEIPT_HEADER1 = RECEIPT_HEADER1;
    }

    public String getMERCHANT_NAME() {
        return this.MERCHANT_NAME;
    }

    public void setMERCHANT_NAME(String MERCHANT_NAME) {
        this.MERCHANT_NAME = MERCHANT_NAME;
    }

    public String getLOCATION() {
        return this.LOCATION;
    }

    public void setLOCATION(String LOCATION) {
        this.LOCATION = LOCATION;
    }

    public String getMOBILE_NUMBER() {
        return this.MOBILE_NUMBER;
    }

    public void setMOBILE_NUMBER(String MOBILE_NUMBER) {
        this.MOBILE_NUMBER = MOBILE_NUMBER;
    }

    public String getRECEIPT_FOOTER1() {
        return this.RECEIPT_FOOTER1;
    }

    public void setRECEIPT_FOOTER1(String RECEIPT_FOOTER1) {
        this.RECEIPT_FOOTER1 = RECEIPT_FOOTER1;
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}