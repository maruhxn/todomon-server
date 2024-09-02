package com.maruhxn.todomon.core.domain.auth.domain;

import com.maruhxn.todomon.core.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false, length = 30)
    private String email;

    @Builder
    public RefreshToken(String payload, String email) {
        this.payload = payload;
        this.email = email;
    }

    public RefreshToken updatePayload(String payload) {
        this.payload = payload;
        return this;
    }
}