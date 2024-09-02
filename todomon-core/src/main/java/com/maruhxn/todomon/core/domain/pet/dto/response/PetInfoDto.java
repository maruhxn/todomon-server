package com.maruhxn.todomon.core.domain.pet.dto.response;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.pet.domain.PetType;
import com.maruhxn.todomon.core.domain.pet.domain.Rarity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PetInfoDto {
    private Long representPetId;
    private int petHouseSize;
    private List<MyPetItem> myPets;

    @Builder
    public PetInfoDto(Long representPetId, int petHouseSize, List<MyPetItem> myPets) {
        this.representPetId = representPetId;
        this.petHouseSize = petHouseSize;
        this.myPets = myPets;
    }

    public static PetInfoDto of(Member member, List<Pet> pets) {
        return PetInfoDto.builder()
                .representPetId(member.getRepresentPet().map(Pet::getId).orElse(null))
                .petHouseSize(member.getPetHouseSize())
                .myPets(pets.stream().map(MyPetItem::from).toList())
                .build();
    }

    @Getter
    @Builder
    public static class MyPetItem {
        private Long id;
        private String name;
        private Rarity rarity;
        private String appearance;
        private String color;
        private int level;
        private double gauge;
        private PetType petType;

        public MyPetItem(Long id, String name, Rarity rarity, String appearance, String color, int level, double gauge, PetType petType) {
            this.id = id;
            this.name = name;
            this.rarity = rarity;
            this.appearance = appearance;
            this.color = color;
            this.level = level;
            this.gauge = gauge;
            this.petType = petType;
        }

        public static MyPetItem from(Pet pet) {
            return MyPetItem.builder()
                    .id(pet.getId())
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
}
