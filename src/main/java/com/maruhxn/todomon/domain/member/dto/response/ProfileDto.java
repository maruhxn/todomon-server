package com.maruhxn.todomon.domain.member.dto.response;

import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.pet.domain.Rarity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileDto {

    private Long id;
    private String username;
    private String email;
    private String profileImageUrl;
    private int level;
    private double gauge;
    private String titleName;
    private String titleColor;
    private RepresentPetItem representPetItem;
    private Long followerCnt;
    private Long followingCnt;

    @Builder
    public ProfileDto(Long id, String username, String email, String profileImageUrl, int level, double gauge, String titleName, String titleColor, RepresentPetItem representPetItem, Long followerCnt, Long followingCnt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.level = level;
        this.gauge = gauge;
        this.titleName = titleName;
        this.titleColor = titleColor;
        this.representPetItem = representPetItem;
        this.followerCnt = followerCnt;
        this.followingCnt = followingCnt;
    }

    public void setRepresentPetItemToNull() {
        this.representPetItem = null;
    }

    @Getter
    @NoArgsConstructor
    public static class RepresentPetItem {
        private Long id;
        private String name;
        private Rarity rarity;
        private String appearance;
        private String color;
        private int level;

        @Builder
        public RepresentPetItem(Long id, String name, Rarity rarity, String appearance, String color, int level) {
            this.id = id;
            this.name = name;
            this.rarity = rarity;
            this.appearance = appearance;
            this.color = color;
            this.level = level;
        }

        public static RepresentPetItem of(Pet representPet) {
            return RepresentPetItem.builder()
                    .id(representPet.getId())
                    .name(representPet.getName())
                    .rarity(representPet.getRarity())
                    .appearance(representPet.getAppearance())
                    .color(representPet.getColor())
                    .level(representPet.getLevel())
                    .build();
        }
    }
}
