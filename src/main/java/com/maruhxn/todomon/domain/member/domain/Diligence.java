package com.maruhxn.todomon.domain.member.domain;

import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
public class Diligence extends BaseEntity {

    @Column(nullable = false)
    @ColumnDefault("0")
    private double gauge = 0;

    @Column(nullable = false)
    @ColumnDefault("1")
    private int level = 1;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    public void setMember(Member member) {
        this.member = member;
    }

    public void increaseGauge(double gauge) {
        this.gauge += gauge;
    }
}
