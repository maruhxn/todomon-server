package com.maruhxn.todomon.core.domain.member.api;

import com.maruhxn.todomon.core.domain.member.application.MemberService;
import com.maruhxn.todomon.core.domain.member.dto.request.UpdateMemberProfileReq;
import com.maruhxn.todomon.core.domain.member.dto.response.MemberSearchRes;
import com.maruhxn.todomon.core.domain.member.dto.response.ProfileRes;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.response.DataResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/search")
    public DataResponse<List<MemberSearchRes>> searchMemberByKey(
            @RequestParam(required = true) String memberNameKey
    ) {
        List<MemberSearchRes> queryResults = memberService.searchMemberByKey(memberNameKey);
        return DataResponse.of("유저 조회 성공", queryResults);
    }

    @GetMapping("/{memberId}")
    public DataResponse<ProfileRes> getProfile(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @PathVariable Long memberId
    ) {
        ProfileRes profile = memberService.getProfile(todomonOAuth2User.getId(), memberId);
        return DataResponse.of("프로필 조회 성공", profile);
    }

    @PatchMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(
            @PathVariable("memberId") Long memberId,
            @ModelAttribute @Valid UpdateMemberProfileReq updateMemberProfileReq
    ) {
        memberService.updateProfile(memberId, updateMemberProfileReq);
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(
            @PathVariable("memberId") Long memberId
    ) {
        memberService.withdraw(memberId);
    }
}
