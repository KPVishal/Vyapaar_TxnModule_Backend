package com.bijlipay.model.dto;

import java.util.List;

public class MailFormat {
    private ForgotPasswordEmail forgotPasswordEmail;
    private List<List<Object>> listList;
    private String[] columnName;
    private String sheetName;

    public ForgotPasswordEmail getForgotPasswordEmail() {
        return forgotPasswordEmail;
    }

    public void setForgotPasswordEmail(ForgotPasswordEmail forgotPasswordEmail) {
        this.forgotPasswordEmail = forgotPasswordEmail;
    }

    public List<List<Object>> getListList() {
        return listList;
    }

    public void setListList(List<List<Object>> listList) {
        this.listList = listList;
    }

    public String[] getColumnName() {
        return columnName;
    }

    public void setColumnName(String[] columnName) {
        this.columnName = columnName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
}
