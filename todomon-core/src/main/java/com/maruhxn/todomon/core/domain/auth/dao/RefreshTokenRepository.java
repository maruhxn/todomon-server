package com.maruhxn.todomon.core.domain.auth.dao;

import com.maruhxn.todomon.core.domain.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByPayload(String payload);

    void deleteAllByUsername(String username);

    Optional<RefreshToken> findByUsername(String email);
}
