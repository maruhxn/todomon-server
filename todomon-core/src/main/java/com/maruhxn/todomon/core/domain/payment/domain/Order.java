package com.maruhxn.todomon.core.domain.payment.domain;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order {

    @Id
    private String merchantUid; // 주문번호

    @Column(nullable = false)
    private Long totalPrice; // 총 가격

    @Column(nullable = false)
    private Long quantity; // 상품 수

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MoneyType moneyType;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.REQUESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "imp_uid")
    private TodomonPayment payment;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Order(String merchantUid, Long totalPrice, Long quantity, MoneyType moneyType, Member member, Item item) {
        this.merchantUid = merchantUid;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.moneyType = moneyType;
        this.member = member;
        this.item = item;
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setPayment(TodomonPayment payment) {
        this.payment = payment;
    }
}
