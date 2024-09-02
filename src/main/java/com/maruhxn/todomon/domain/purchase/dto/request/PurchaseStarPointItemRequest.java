package com.maruhxn.todomon.domain.purchase.dto.request;


import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.purchase.domain.StarPointPaymentHistory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PurchaseStarPointItemRequest {

    @NotEmpty(message = "주문 번호는 비어있을 수 없습니다.")
    private String merchant_uid;

    @NotNull(message = "아이템 아이디는 비어있을 수 없습니다.")
    private Long itemId;

    @NotNull(message = "수량은 비어있을 수 없습니다.")
    @Min(value = 0, message = "수량은 양수값이어야 합니다.")
    private Long quantity;

    @NotNull(message = "결제 금액 비어있을 수 없습니다.")
    @Min(value = 0, message = "결제 금액은 양수값이어야 합니다.")
    private Long amount;

    @Builder
    public PurchaseStarPointItemRequest(String merchant_uid, Long itemId, Long quantity, Long amount) {
        this.merchant_uid = merchant_uid;
        this.itemId = itemId;
        this.quantity = quantity;
        this.amount = amount;
    }

    public static StarPointPaymentHistory toEntity(Member member, PurchaseStarPointItemRequest request) {
        return StarPointPaymentHistory.builder()
                .merchantUid(request.getMerchant_uid())
                .itemId(request.getItemId())
                .quantity(request.getQuantity())
                .amount(request.getAmount())
                .member(member)
                .build();
    }
}
