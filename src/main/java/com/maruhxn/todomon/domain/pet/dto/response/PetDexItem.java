package com.maruhxn.todomon.domain.pet.dto.response;

import com.maruhxn.todomon.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class PetDexItem {
    private String name;
    private Rarity rarity;
    private String appearance;

    @Builder
    public PetDexItem(String name, Rarity rarity, String appearance) {
        this.name = name;
        this.rarity = rarity;
        this.appearance = appearance;
    }

    public static PetDexItem from(CollectedPet collectedPet) {
        return PetDexItem.builder()
                .name(collectedPet.getName())
                .appearance(collectedPet.getAppearance())
                .rarity(collectedPet.getRarity())
                .build();
    }
}
