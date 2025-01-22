package com.maruhxn.todomon.core.domain.payment.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class WebhookPayload {

    @NotEmpty(message = "주문 번호는 비어있을 수 없습니다.")
    private String merchant_uid;
    @NotEmpty(message = "결제 아이디는 비어있을 수 없습니다.")
    private String imp_uid;
    private String status;
    private String cancellation_id;

    @Builder
    public WebhookPayload(String merchant_uid, String imp_uid, String status, String cancellation_id) {
        this.merchant_uid = merchant_uid;
        this.imp_uid = imp_uid;
        this.status = status;
        this.cancellation_id = cancellation_id;
    }
}
