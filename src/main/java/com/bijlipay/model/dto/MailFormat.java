package com.bijlipay.model.dto;

import java.util.List;

public class MailFormat {
    private ForgotPasswordEmail forgotPasswordEmail;
    private List<List<Object>> listList;
    private String[] columnName;
    private String sheetName;

    public ForgotPasswordEmail getForgotPasswordEmail() {
        return this.forgotPasswordEmail;
    }

    public void setForgotPasswordEmail(ForgotPasswordEmail forgotPasswordEmail) {
        this.forgotPasswordEmail = forgotPasswordEmail;
    }

    public List<List<Object>> getListList() {
        return this.listList;
    }

    public void setListList(List<List<Object>> listList) {
        this.listList = listList;
    }

    public String[] getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String[] columnName) {
        this.columnName = columnName;
    }

    public String getSheetName() {
        return this.sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
}