package com.maruhxn.todomon.core.domain.member.application;

import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.dto.request.UpdateMemberProfileReq;
import com.maruhxn.todomon.core.domain.member.dto.response.MemberSearchRes;
import com.maruhxn.todomon.core.domain.member.dto.response.ProfileRes;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.member.implement.MemberWriter;
import com.maruhxn.todomon.core.global.auth.checker.IsMeOrAdmin;
import com.maruhxn.todomon.core.global.auth.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberReader memberReader;
    private final MemberWriter memberWriter;


    @Transactional(readOnly = true)
    public List<MemberSearchRes> searchMemberByKey(String key) {
        log.debug("유저 검색 === 키: {}", key);
        return memberReader.search(key);
    }

    @Transactional(readOnly = true)
    public UserInfo getMemberInfo(Long memberId) {
        log.debug("로그인 유저 정보 조회: 유저 아이디: {}", memberId);
        return UserInfo.from(memberReader.findById(memberId));
    }

    @Transactional(readOnly = true)
    public ProfileRes getProfile(Long loginMemberId, Long targetMemberId) {
        log.debug("유저 프로필 조회: 로그인 유저 아이디: {}, 대상 유저 아이디: {}", loginMemberId, targetMemberId);
        return memberReader.getProfile(loginMemberId, targetMemberId);
    }

    @Transactional
    @IsMeOrAdmin
    public void updateProfile(Long memberId, UpdateMemberProfileReq req) {
        log.info("유저 정보 수정 === 유저 아이디: {}, 요청 정보: {}", memberId, req);
        Member member = memberReader.findById(memberId);
        memberWriter.modify(member, req);
    }

    @Transactional
    @IsMeOrAdmin
    public void withdraw(Long memberId) {
        Member member = memberReader.findById(memberId);
        log.info("회원 탈퇴 === 유저 아이디: {}, 유저명: {}, 유저 이메일: {}", member.getId(), member.getUsername(), member.getEmail());
        memberWriter.withdraw(member);
    }
}
