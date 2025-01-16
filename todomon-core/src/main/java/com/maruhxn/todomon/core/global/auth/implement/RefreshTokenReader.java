package com.maruhxn.todomon.core.global.auth.implement;

import com.maruhxn.todomon.core.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.core.domain.auth.domain.RefreshToken;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenReader {

    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> findOptionalByUsername(String username) {
        return refreshTokenRepository.findByUsername(username);
    }

    public RefreshToken findByPayload(String payload) {
        return refreshTokenRepository.findByPayload(payload)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_REFRESH_TOKEN));
    }
}
