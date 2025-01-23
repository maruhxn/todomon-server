package com.maruhxn.todomon.core.domain.purchase.dto.request;


import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.purchase.domain.StarPointPaymentHistory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class PurchaseStarPointItemReq {

    @NotNull(message = "아이템 아이디는 비어있을 수 없습니다.")
    private Long itemId;

    @NotNull(message = "수량은 비어있을 수 없습니다.")
    @Min(value = 0, message = "수량은 양수값이어야 합니다.")
    private Long quantity;

    @NotNull(message = "결제 금액 비어있을 수 없습니다.")
    @Min(value = 0, message = "결제 금액은 양수값이어야 합니다.")
    private Long amount;

    @Builder
    public PurchaseStarPointItemReq(Long itemId, Long quantity, Long amount) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.amount = amount;
    }

    public StarPointPaymentHistory toEntity(Member member) {
        return StarPointPaymentHistory.builder()
                .itemId(this.getItemId())
                .quantity(this.getQuantity())
                .amount(this.getAmount())
                .member(member)
                .build();
    }
}
