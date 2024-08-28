package com.maruhxn.todomon.domain.purchase.domain;

import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment")
public class TodomonPayment extends BaseEntity {


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @Column(nullable = false, unique = true)
    private String impUid; // 포트원 결제 id

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Long amount;

    @Builder
    public TodomonPayment(Order order, String impUid, PaymentStatus status, Long amount) {
        this.order = order;
        this.impUid = impUid;
        this.status = status;
        this.amount = amount;
    }
}
