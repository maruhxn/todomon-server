package com.maruhxn.todomon.infra.payment.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PrepareData {
    private String merchant_uid;
    private BigDecimal amount;
    private String currency;

    @Builder
    public PrepareData(String merchant_uid, BigDecimal amount, String currency) {
        this.merchant_uid = merchant_uid;
        this.amount = amount;
        this.currency = currency;
    }

}
