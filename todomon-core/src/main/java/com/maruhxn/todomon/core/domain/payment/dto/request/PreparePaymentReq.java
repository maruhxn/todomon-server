package com.maruhxn.todomon.core.domain.payment.dto.request;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.payment.domain.Order;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PreparePaymentReq {

    @NotEmpty(message = "주문 번호는 비어있을 수 없습니다.")
    private String merchant_uid; // 주문번호

    @NotNull(message = "아이템 아이디는 비어있을 수 없습니다.")
    private Long itemId;

    @NotNull(message = "수량은 비어있을 수 없습니다.")
    @Min(value = 0, message = "수량은 양수값이어야 합니다.")
    private Long quantity;

    @NotNull(message = "결제 금액은 비어있을 수 없습니다.")
    @Min(value = 0, message = "결제 금액은 양수값이어야 합니다.")
    private BigDecimal amount;

    @Builder
    public PreparePaymentReq(String merchant_uid, BigDecimal amount, Long quantity, Long itemId) {
        this.merchant_uid = merchant_uid;
        this.amount = amount;
        this.quantity = quantity;
        this.itemId = itemId;
    }

    public Order toOrder(Item item, Member member) {
        return Order.builder()
                .item(item)
                .member(member)
                .totalPrice(item.getPrice() * this.quantity)
                .quantity(this.quantity)
                .merchantUid(this.merchant_uid)
                .moneyType(item.getMoneyType())
                .build();
    }
}
