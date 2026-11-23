package com.bijlipay.model.dto;

import org.springframework.data.domain.Page;

public class PagedResponseWithAmountDTO {
    private Page<SwitchTransactionsDto> page;
    private double amount;

    public PagedResponseWithAmountDTO(Page<SwitchTransactionsDto> page, double amount) {
        this.page = page;
        this.amount = amount;
    }

    public Page<SwitchTransactionsDto> getPage() {
        return this.page;
    }

    public void setPage(Page<SwitchTransactionsDto> page) {
        this.page = page;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}