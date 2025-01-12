package com.maruhxn.todomon.core.domain.purchase.domain;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(nullable = false)
    private Long totalPrice; // 총 가격

    @Column(nullable = false)
    private Long quantity; // 상품 수

    @Column(nullable = false, unique = true)
    private String merchantUid; // 주문번호

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MoneyType moneyType;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.REQUEST_PAYMENT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

    @Builder
    public Order(Long totalPrice, Long quantity, String merchantUid, MoneyType moneyType, Member member, Item item) {
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.merchantUid = merchantUid;
        this.moneyType = moneyType;
        this.member = member;
        this.item = item;
    }

    public static Order of(Item item, Member member, Long quantity, String merchantUid) {
        return Order.builder()
                .item(item)
                .member(member)
                .totalPrice(item.getPrice() * quantity)
                .quantity(quantity)
                .merchantUid(merchantUid)
                .moneyType(item.getMoneyType())
                .build();
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

}
