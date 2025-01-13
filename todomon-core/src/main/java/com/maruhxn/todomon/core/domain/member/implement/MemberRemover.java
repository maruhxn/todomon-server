package com.maruhxn.todomon.core.domain.member.implement;

import com.maruhxn.todomon.core.domain.auth.dao.RefreshTokenRepository;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.infra.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberRemover {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FileService fileService;

    public void withdraw(Member member) {
        fileService.deleteFile(member.getProfileImageUrl());
        memberRepository.delete(member);
        refreshTokenRepository.deleteAllByEmail(member.getEmail());
    }
}
