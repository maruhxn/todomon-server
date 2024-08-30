package com.maruhxn.todomon.domain.member.api;

import com.maruhxn.todomon.domain.member.application.TitleNameService;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTitleName(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        titleNameService.deleteTitleName(todomonOAuth2User.getMember());
    }
}
