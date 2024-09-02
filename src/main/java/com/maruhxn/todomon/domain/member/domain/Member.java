package com.maruhxn.todomon.domain.member.domain;

import com.maruhxn.todomon.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.domain.pet.domain.CollectedPet;
import com.maruhxn.todomon.domain.pet.domain.Pet;
import com.maruhxn.todomon.domain.social.domain.Follow;
import com.maruhxn.todomon.domain.social.domain.StarTransaction;
import com.maruhxn.todomon.domain.todo.domain.TodoAchievementHistory;
import com.maruhxn.todomon.global.auth.model.Role;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2Provider;
import com.maruhxn.todomon.global.auth.model.provider.OAuth2ProviderUser;
import com.maruhxn.todomon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.util.StringUtils;

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
    private Long scheduledReward = 0L;

    @ColumnDefault("0")
    private Long dailyAchievementCnt = 0L;

    @ColumnDefault("0")
    private Long foodCnt = 0L;

    @ColumnDefault("0")
    private boolean isSubscribed = false;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Diligence diligence;

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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoAchievementHistory> todoAchievementHistories = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private TitleName titleName;

    // 유저는 여러 인벤토리 아이템을 가질 수 있음
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryItem> inventoryItems = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "represent_pet_id")
    private Pet representPet;

    @Builder
    public Member(String username, String email, OAuth2Provider provider, String providerId, String profileImageUrl, Role role) {
        this.username = username;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
    }

    public void updateIsSubscribed(boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    public void updateByOAuth2Info(OAuth2ProviderUser oAuth2ProviderUser) {
        this.username = oAuth2ProviderUser.getUsername();
        this.profileImageUrl = oAuth2ProviderUser.getProfileImageUrl();
    }

    public void initDiligence() {
        Diligence diligence = new Diligence();
        this.diligence = diligence;
        diligence.setMember(this);
    }

    public void addFood(int foodCnt) {
        this.foodCnt += foodCnt;
    }

    public void initScheduledReward() {
        this.scheduledReward = 0L;
    }

    public void addScheduledReward(long reward) {
        this.scheduledReward += reward;
    }

    public void subtractScheduledReward(long reward) {
        this.scheduledReward -= reward;
    }

    public void decreaseFoodCnt(Long foodCnt) {
        this.foodCnt -= foodCnt;
    }

    public void addStar(Long starCnt) {
        this.starPoint += starCnt;
    }

    public void expandPetHouse() {
        this.petHouseSize += 1;
    }

    /* 연관관계 메서드 */

    public void setTitleName(TitleName titleName) {
        this.titleName = titleName;
        titleName.setMember(this);
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
        this.dailyAchievementCnt = 0L;
    }

    public void updateProfile(String username, String newProfileImageUrl) {
        if (StringUtils.hasText(username)) {
            this.username = username;
        }

        if (StringUtils.hasText(newProfileImageUrl)) {
            this.profileImageUrl = newProfileImageUrl;
        }
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
}
