package com.maruhxn.todomon.core.domain.member.domain;

import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.core.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.core.domain.pet.domain.Pet;
import com.maruhxn.todomon.core.domain.social.domain.Follow;
import com.maruhxn.todomon.core.domain.social.domain.StarTransaction;
import com.maruhxn.todomon.core.global.auth.model.Role;
import com.maruhxn.todomon.core.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.core.global.common.BaseEntity;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Enumerated(EnumType.STRING)
    private OAuth2Provider provider;

    @Column(unique = true)
    private String providerId;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ColumnDefault("3")
    private Integer petHouseSize = 3;

    @ColumnDefault("0")
    private Long starPoint = 0L;

    @ColumnDefault("0")
    private int scheduledReward = 0;

    @ColumnDefault("0")
    private int dailyAchievementCnt = 0;

    @ColumnDefault("0")
    private Long foodCnt = 0L;

    @ColumnDefault("0")
    private boolean isSubscribed = false;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "diligence_id", referencedColumnName = "id")
    private Diligence diligence;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "title_name_id", referencedColumnName = "id")
    private TitleName titleName;

    @OneToOne(fetch = FetchType.LAZY) // cascade 설정 시 pet도 삭제되니 따로 추가하지 않음.
    @JoinColumn(name = "represent_pet_id")
    private Pet representPet;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pet> pets = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectedPet> collectedPets = new ArrayList<>();

    // 팔로워
    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers = new ArrayList<>();

    // 팔로잉
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings = new ArrayList<>();

    // 보낸 star
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StarTransaction> sentStars = new ArrayList<>();

    // 받은 star
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StarTransaction> receivedStars = new ArrayList<>();

    // 유저는 여러 인벤토리 아이템을 가질 수 있음
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    @Builder
    public Member(String username, String email, OAuth2Provider provider, String providerId, String profileImageUrl, Role role) {
        this.username = username;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImageUrl = profileImageUrl;
        this.role = role;

        this.initDiligence();
    }

    public void updateIsSubscribed(boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    public void initDiligence() {
        Diligence diligence = new Diligence();
        this.diligence = diligence;
        diligence.setMember(this);
    }

    public void addFood(int foodCnt) {
        this.foodCnt += foodCnt;
    }

    public void resetScheduledReward() {
        this.scheduledReward = 0;
    }

    public void addScheduledReward(int reward) {
        this.scheduledReward += reward;
    }

    public void subtractScheduledReward(int reward) {
        this.scheduledReward -= reward;
    }

    public void decreaseFoodCnt(Long foodCnt) {
        this.foodCnt -= foodCnt;
    }

    public void addStar(int starCnt) {
        this.starPoint += starCnt;
    }

    public void expandPetHouse() {
        this.petHouseSize += 1;
    }

    /* 연관관계 메서드 */

    public void setTitleName(TitleName titleName) {
        this.titleName = titleName;
        if (titleName != null) titleName.setMember(this);
    }

    public void addPet(Pet pet) {
        this.pets.add(pet);
        pet.setOwner(this);
    }

    public void addCollection(CollectedPet collectedPet) {
        this.collectedPets.add(collectedPet);
        collectedPet.setMember(this);
    }

    public void addItemToInventory(InventoryItem inventoryItem) {
        this.inventoryItems.add(inventoryItem);
        inventoryItem.setMember(this);
    }

    public void addDailyAchievementCnt(int cnt) {
        this.dailyAchievementCnt += cnt;
    }

    public void resetDailyAchievement() {
        this.dailyAchievementCnt = 0;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateProfileImageUrl(String newProfileImageUrl) {
        this.profileImageUrl = newProfileImageUrl;
    }

    public Optional<Pet> getRepresentPet() {
        return Optional.ofNullable(this.representPet);
    }

    public void setRepresentPet(Pet pet) {
        this.representPet = pet;
    }

    public void addStarPoint(Long refundAmount) {
        this.starPoint += refundAmount;
    }

    public void subtractStarPoint(Long totalPrice) {
        this.starPoint -= totalPrice;
    }

    public void validatePetHouseSpace() {
        if (this.getPetHouseSize() <= this.getPets().size()) {
            throw new BadRequestException(ErrorCode.NO_SPACE_PET_HOUSE);
        }
    }

    public void validateFoodCnt(Long foodCnt) {
        if (this.foodCnt < foodCnt)
            throw new BadRequestException(ErrorCode.OVER_FOOD_CNT);
    }
}
