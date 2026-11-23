package com.bijlipay.model.dto;

import java.util.List;

public class TransactionRequestDto {
    int txnStatus;
    List<String> txnType;
    int dateRange;

    public int getTxnStatus() {
        return this.txnStatus;
    }

    public void setTxnStatus(int txnStatus) {
        this.txnStatus = txnStatus;
    }

    public List<String> getTxnType() {
        return this.txnType;
    }

    public void setTxnType(List<String> txnType) {
        this.txnType = txnType;
    }

    public int getDateRange() {
        return this.dateRange;
    }

    public void setDateRange(int dateRange) {
        this.dateRange = dateRange;
    }
}