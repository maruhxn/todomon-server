package com.maruhxn.todomon.core.domain.payment.dto.message;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaidOrderMessage {

    private Long memberId;
    private String merchantUid;

    @Builder
    public PaidOrderMessage(Long memberId, String merchantUid) {
        this.memberId = memberId;
        this.merchantUid = merchantUid;
    }
}
