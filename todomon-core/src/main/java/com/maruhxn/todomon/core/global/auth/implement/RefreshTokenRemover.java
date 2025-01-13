package com.maruhxn.todomon.core.global.auth.implement;

import com.maruhxn.todomon.core.domain.auth.dao.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenRemover {

    private final RefreshTokenRepository refreshTokenRepository;

    public void removeAllByEmail(String email) {
        refreshTokenRepository.deleteAllByEmail(email);
    }
}
