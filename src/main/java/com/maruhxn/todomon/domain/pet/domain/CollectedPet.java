package com.maruhxn.todomon.domain.pet.domain;

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
public class CollectedPet extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    @Column(length = 20, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private PetType petType;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    @Lob
    private String appearance;

    @Builder
    public CollectedPet(PetType petType, Rarity rarity, int evolutionCnt) {
        this.name = petType.getEvolutionStage(evolutionCnt).getName();
        this.petType = petType;
        this.rarity = rarity;
        this.appearance = petType.getEvolutionStage(evolutionCnt).getForm();
    }

    public void setMember(Member member) {
        this.member = member;
        member.getCollectedPets().add(this);
    }
}
