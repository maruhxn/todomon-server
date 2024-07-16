package com.maruhxn.todomon.domain.member.domain;

import com.maruhxn.todomon.domain.pet.domain.Pet;
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

import java.util.ArrayList;
import java.util.List;

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
    private Long foodCnt = 0L;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Diligence diligence;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Pet> pets = new ArrayList<>();

    @Builder
    public Member(String username, String email, OAuth2Provider provider, String providerId, String profileImageUrl, Role role) {
        this.username = username;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
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

    public void decreaseFoodCnt(int foodCnt) {
        this.foodCnt -= foodCnt;
    }

    /* 연관관계 메서드 */
    public void addPet(Pet pet) {
        this.pets.add(pet);
        pet.setOwner(this);
    }
}
