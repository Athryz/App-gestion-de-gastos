package com.appgestor.dto;

import java.math.BigDecimal;

public class TransactionDTO {

    private Long originId;
    private Long destinyId;
    private BigDecimal amount;

   
    public TransactionDTO() {
    }

  
    public Long getOriginId() {
        return originId;
    }

    public void setOriginId(Long originId) {
        this.originId = originId;
    }

    public Long getDestinyId() {
        return destinyId;
    }

    public void setDestinyId(Long destinyId) {
        this.destinyId = destinyId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}