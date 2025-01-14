package com.maruhxn.todomon.core.global.auth.implement;

import com.maruhxn.todomon.core.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.core.domain.auth.domain.RefreshToken;
import com.maruhxn.todomon.core.global.auth.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenWriter {

    private final RefreshTokenReader refreshTokenReader;
    private final RefreshTokenRepository refreshTokenRepository;

    public void upsertRefreshToken(TokenDto tokenDto) {
        refreshTokenReader.findOptionalByEmail(tokenDto.getEmail())
                .ifPresentOrElse(
                        // 있다면 새토큰 발급후 업데이트
                        token -> token.updatePayload(tokenDto.getRefreshToken()),
                        // 없다면 새로 만들고 DB에 저장
                        () -> this.create(tokenDto)
                );
    }

    private void create(TokenDto tokenDto) {
        refreshTokenRepository.save(new RefreshToken(tokenDto.getRefreshToken(), tokenDto.getEmail()));
    }

    public void removeAllByEmail(String email) {
        refreshTokenRepository.deleteAllByEmail(email);
    }
}
