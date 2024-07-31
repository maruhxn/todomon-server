package com.maruhxn.todomon.domain.pet.dto.response;

import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.PetType;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PetItem {
    private String name;
    private Rarity rarity;
    private String appearance;
    private String color;
    private int level;
    private double gauge;
    private PetType petType;

    @Builder
    public PetItem(String name, Rarity rarity, String appearance, String color, int level, double gauge, PetType petType) {
        this.name = name;
        this.rarity = rarity;
        this.appearance = appearance;
        this.color = color;
        this.level = level;
        this.gauge = gauge;
        this.petType = petType;
    }

    public static PetItem from(Pet pet) {
        return PetItem.builder()
                .name(pet.getName())
                .color(pet.getColor())
                .rarity(pet.getRarity())
                .appearance(pet.getAppearance())
                .level(pet.getLevel())
                .gauge(pet.getGauge())
                .petType(pet.getPetType())
                .build();
    }
}
