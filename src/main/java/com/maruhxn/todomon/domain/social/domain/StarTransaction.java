package com.maruhxn.todomon.domain.social.domain;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StarTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false, referencedColumnName = "id")
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false, referencedColumnName = "id")
    private Member receiver;

    @Enumerated(EnumType.STRING)
    private StarTransactionStatus status;

    private StarTransaction(Member sender, Member receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = StarTransactionStatus.SENT;
    }

    public static StarTransaction createTransaction(Member sender, Member receiver) {
        return new StarTransaction(sender, receiver);
    }

    public void updateStatus(StarTransactionStatus status) {
        this.status = status;
    }
}
