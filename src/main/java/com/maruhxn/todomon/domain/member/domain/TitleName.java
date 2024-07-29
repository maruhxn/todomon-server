package com.maruhxn.todomon.domain.member.domain;

import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TitleName extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    @Builder
    public TitleName(String name, String color, Member member) {
        this.name = name;
        this.color = color;
        this.member = member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void update(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
