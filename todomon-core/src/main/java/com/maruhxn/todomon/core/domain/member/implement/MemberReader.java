package com.maruhxn.todomon.core.domain.member.implement;

import com.maruhxn.todomon.core.domain.member.dao.MemberQueryRepository;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.dto.response.MemberSearchRes;
import com.maruhxn.todomon.core.domain.member.dto.response.ProfileRes;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberReader {

    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;

    public List<MemberSearchRes> search(String key) {
        return memberQueryRepository.findMemberByNameKey(key);
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public Member findById(Long id, String errMessage) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER, errMessage));
    }

    public Optional<Member> findOptionalByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public ProfileRes getProfile(Long loginMemberId, Long targetMemberId) {
        return memberQueryRepository.getMemberProfileById(loginMemberId, targetMemberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public Member findMemberWithPetsById(Long id) {
        return memberRepository.findMemberWithPets(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public Member findMemberWithRepresentPet(Long id) {
        return memberRepository.findMemberWithRepresentPet(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
    }

    public Member findMemberWithDiligenceUsingLock(Long memberId) {
        return memberRepository.findMemberWithDiligenceUsingLock(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));
    }
}
