package com.maruhxn.todomon.domain.member.domain;

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

    private Long scheduledReward = 0L;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Diligence diligence;

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

    public void initScheduledReward() {
        this.scheduledReward = 0L;
    }

    public void addScheduledReward(long reward) {
        this.scheduledReward += reward;
    }

    public void subtractScheduledReward(long reward) {
        this.scheduledReward -= reward;
    }
}
