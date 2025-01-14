package com.maruhxn.todomon.core.domain.pet.dto.response;

import com.maruhxn.todomon.core.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.core.domain.pet.domain.PetType;
import com.maruhxn.todomon.core.domain.pet.domain.Rarity;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class PetDexItem {
    private String name;
    private Rarity rarity;
    private String appearance;
    private String color;

    @Builder
    public PetDexItem(String name, Rarity rarity, String appearance) {
        this.name = name;
        this.rarity = rarity;
        this.color = rarity.getColor();
        this.appearance = appearance;

    }

    public static PetDexItem from(CollectedPet collectedPet) {
        return PetDexItem.builder()
                .name(collectedPet.getName())
                .appearance(collectedPet.getAppearance())
                .rarity(collectedPet.getRarity())
                .build();
    }

    public static PetDexItem of(PetType.EvolutionStage stage, Rarity rarity) {
        return PetDexItem.builder()
                .name(stage.getName())
                .appearance(stage.getForm())
                .rarity(rarity)
                .build();
    }
}
