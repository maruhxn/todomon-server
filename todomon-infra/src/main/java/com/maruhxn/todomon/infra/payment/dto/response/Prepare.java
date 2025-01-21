package com.maruhxn.todomon.infra.payment.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class Prepare {
    private String merchant_uid;
    private BigDecimal amount;
    private String currency;
}
