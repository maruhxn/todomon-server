package com.maruhxn.todomon.domain.member.api;

import com.maruhxn.todomon.domain.member.application.MemberService;
import com.maruhxn.todomon.domain.member.dto.request.UpdateMemberProfileReq;
import com.maruhxn.todomon.domain.member.dto.response.ProfileDto;
import com.maruhxn.todomon.global.common.dto.response.DataResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    public DataResponse<ProfileDto> getProfile(
            @PathVariable("memberId") Long memberId
    ) {
        ProfileDto profile = memberService.getProfile(memberId);
        return DataResponse.of("프로필 조회 성공", profile);
    }

    @PatchMapping("/{memberId}")
    @PreAuthorize("@authChecker.isMeOrAdmin(#memberId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(
            @PathVariable("memberId") Long memberId,
            @ModelAttribute @Valid UpdateMemberProfileReq updateMemberProfileReq
    ) {
        memberService.updateProfile(memberId, updateMemberProfileReq);
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("@authChecker.isMeOrAdmin(#memberId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(
            @PathVariable("memberId") Long memberId
    ) {
        log.info("회원 탈퇴 | memberId={}", memberId);
        memberService.withdraw(memberId);
    }
}
