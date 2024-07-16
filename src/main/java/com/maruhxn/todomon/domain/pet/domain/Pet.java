package com.maruhxn.todomon.domain.pet.domain;

import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pet extends BaseEntity {
    @Column(length = 20, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    @Lob
    private String appearance;

    @ColumnDefault("0")
    private int evolutionCnt = 0;

    @ColumnDefault("1")
    private int level = 1;

    @ColumnDefault("0")
    private double gauge = 0;

    @Enumerated(EnumType.STRING)
    private PetType petType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    @Builder
    public Pet(String name, Rarity rarity, PetType petType) {
        this.name = name != null ? name : petType.getName();
        this.rarity = rarity;
        this.petType = petType;
        this.appearance = petType.getEvolutionStage(0).getForm();
    }

    public void levelUp() {
        this.level++;
        if (level % 30 == 0 && evolutionCnt < petType.getEvolutionaryCnt()) {
            evolution();
        }
    }

    private void evolution() {
        this.evolutionCnt++;
        this.appearance = petType.getEvolutionStage(this.evolutionCnt).getForm();
    }

    public void increaseGauge(double gauge) {
        this.gauge += gauge;
        while (this.gauge >= 100) { // 게이지가 100 이상이 될 경우, 레벨이 증가한다.
            levelUp();
            this.gauge -= 100;
        }
    }

    public void setOwner(Member member) {
        this.member = member;
    }
}
