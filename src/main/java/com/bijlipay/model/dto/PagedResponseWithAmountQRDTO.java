package com.bijlipay.model.dto;

import org.springframework.data.domain.Page;

public class PagedResponseWithAmountQRDTO {

    private Page<AxisQrTransactionDto> page;
    private double amount;


    public PagedResponseWithAmountQRDTO(Page<AxisQrTransactionDto> page, double amount) {
        this.page = page;
        this.amount = amount;
    }

    public Page<AxisQrTransactionDto> getPage() {
        return page;
    }

    public void setPage(Page<AxisQrTransactionDto> page) {
        this.page = page;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
