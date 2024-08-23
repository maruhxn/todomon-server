package com.maruhxn.todomon.domain.member.api;

import com.maruhxn.todomon.domain.member.application.TitleNameService;
import com.maruhxn.todomon.domain.member.dto.request.CreateTitleNameReq;
import com.maruhxn.todomon.domain.member.dto.request.UpdateTitleNameReq;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.common.dto.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 타 유저의 칭호관련 API에는 접근 불가능.
 * 구독을 한 유저는 칭호를 '생성'할 수 있음
 * 한번 칭호를 생성한 이후, '수정' 혹은 '삭제'만 가능
 */
@RestController
@RequestMapping("/api/members/titleNames/my")
@RequiredArgsConstructor
public class MyTitleNameController {

    private final TitleNameService titleNameService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse createTitleName(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestBody @Valid CreateTitleNameReq req
    ) {
        titleNameService.createTitleName(todomonOAuth2User.getMember(), req);
        return new BaseResponse("칭호 생성 성공");
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTitleName(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestBody @Valid UpdateTitleNameReq req
    ) {
        titleNameService.updateTitleName(todomonOAuth2User.getMember(), req);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTitleName(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        titleNameService.deleteTitleName(todomonOAuth2User.getMember());
    }
}
