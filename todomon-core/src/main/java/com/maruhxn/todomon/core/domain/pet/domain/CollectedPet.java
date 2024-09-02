package com.maruhxn.todomon.core.domain.pet.domain;

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

    public static CollectedPet of(Pet pet) {
        return CollectedPet.builder()
                .rarity(pet.getRarity())
                .evolutionCnt(pet.getEvolutionCnt())
                .petType(pet.getPetType())
                .build();
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
