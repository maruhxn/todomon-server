package com.maruhxn.todomon.domain.member.api;

import com.maruhxn.todomon.domain.member.application.TitleNameService;
import com.maruhxn.todomon.domain.member.dto.request.CreateTitleNameReq;
import com.maruhxn.todomon.domain.member.dto.request.UpdateTitleNameReq;
import com.maruhxn.todomon.domain.member.dto.response.TitleNameItem;
import com.maruhxn.todomon.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.global.common.dto.response.BaseResponse;
import com.maruhxn.todomon.global.common.dto.response.DataResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/titleNames")
@RequiredArgsConstructor
public class TitleNameController {

    private final TitleNameService titleNameService;

    @GetMapping
    public DataResponse<TitleNameItem> getTitleName(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        TitleNameItem titleName = titleNameService.getTitleName(todomonOAuth2User.getMember());
        return DataResponse.of("칭호 조회 성공", titleName);
    }

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
