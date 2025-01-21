package com.maruhxn.todomon.core.domain.payment.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentReq {

    @NotEmpty(message = "주문 번호는 비어있을 수 없습니다.")
    private String merchant_uid;
    @NotEmpty(message = "결제 아이디는 비어있을 수 없습니다.")
    private String imp_uid;

    @Builder
    public PaymentReq(String merchant_uid, String imp_uid) {
        this.merchant_uid = merchant_uid;
        this.imp_uid = imp_uid;
    }
}
