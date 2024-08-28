package com.maruhxn.todomon.domain.purchase.domain;

import com.maruhxn.todomon.domain.member.domain.Member;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @Column(nullable = false, unique = true)
    private String impUid; // 포트원 결제 id

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.OK;

    private Long amount;

    @Builder
    public TodomonPayment(Member member, Order order, String impUid, Long amount) {
        this.member = member;
        this.order = order;
        this.impUid = impUid;
        this.amount = amount;
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }
}
