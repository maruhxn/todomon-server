package com.maruhxn.todomon.core.domain.payment.domain;

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
@Table(name = "payment")
public class TodomonPayment {

    @Id
    private String impUid; // 포트원 결제 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.OK;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    protected TodomonPayment(Member member, String impUid, Long amount) {
        this.member = member;
        this.impUid = impUid;
        this.amount = amount;
    }

    public static TodomonPayment of(Order order, String impUid) {
        return TodomonPayment.builder()
                .member(order.getMember())
                .impUid(impUid)
                .amount(order.getTotalPrice())
                .build();
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }
}
