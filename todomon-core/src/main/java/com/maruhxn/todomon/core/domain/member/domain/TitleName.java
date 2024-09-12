package com.maruhxn.todomon.core.domain.member.domain;

import com.maruhxn.todomon.core.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TitleName extends BaseEntity {

    @Column(nullable = false, length = 5)
    private String name;

    @Column(nullable = false)
    private String color;

    @OneToOne(mappedBy = "titleName", fetch = FetchType.LAZY)
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
