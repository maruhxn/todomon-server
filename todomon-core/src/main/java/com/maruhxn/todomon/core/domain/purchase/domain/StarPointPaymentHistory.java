package com.maruhxn.todomon.core.domain.purchase.domain;

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
public class StarPointPaymentHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    @Column(nullable = false, unique = true)
    private String merchantUid;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.OK;

    @Builder
    public StarPointPaymentHistory(Member member, String merchantUid, Long itemId, Long quantity, Long amount, PaymentStatus status) {
        this.member = member;
        this.merchantUid = merchantUid;
        this.itemId = itemId;
        this.quantity = quantity;
        this.amount = amount;
        this.status = status;
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

}
