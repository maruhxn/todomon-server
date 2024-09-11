package com.maruhxn.todomon.core.domain.member.api;

import com.maruhxn.todomon.core.domain.member.dao.TitleNameRepository;
import com.maruhxn.todomon.core.domain.member.domain.TitleName;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.util.ControllerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.maruhxn.todomon.core.global.auth.application.JwtProvider.BEARER_PREFIX;
import static com.maruhxn.todomon.core.global.common.Constants.ACCESS_TOKEN_HEADER;
import static com.maruhxn.todomon.core.global.common.Constants.REFRESH_TOKEN_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("[Integration] - TitleName")
class TitleNameIntegrationTest extends ControllerIntegrationTestSupport {

    static final String TITLENAME_BASE_URL = "/api/members/titleNames/my";

    @Autowired
    TitleNameRepository titleNameRepository;

//    @Test
//    @DisplayName("POST /api/members/titleNames/my 요청 시 로그인 한 유저의 칭호를 생성한다.")
//    void createTitleName() throws Exception {
//        // given
//        UpsertTitleNameRequest req = UpsertTitleNameRequest.builder()
//                .name("name")
//                .color("#000000")
//                .build();
//
//        // when / then
//        mockMvc.perform(
//                        post(TITLENAME_BASE_URL)
//                                .content(objectMapper.writeValueAsString(req))
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
//                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
//                )
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("code").value("OK"))
//                .andExpect(jsonPath("message").value("칭호 생성 성공"));
//    }

//    @Test
//    @DisplayName("POST /api/members/titleNames/my 요청 시 칭호명이 5글자를 넘으면 400 에러를 반환한다.")
//    void createTitleNameReturn400WithLongTitleName() throws Exception {
//        // given
//        UpsertTitleNameRequest req = UpsertTitleNameRequest.builder()
//                .name("toolongname")
//                .color("#000000")
//                .build();
//
//        // when / then
//        mockMvc.perform(
//                        post(TITLENAME_BASE_URL)
//                                .content(objectMapper.writeValueAsString(req))
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
//                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
//                )
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("code").value(ErrorCode.VALIDATION_ERROR.name()))
//                .andExpect(jsonPath("message").value(ErrorCode.VALIDATION_ERROR.getMessage()));
//    }
//
//    @Test
//    @DisplayName("PATCH /api/members/titleNames/my 요청 시 칭호를 수정한다.")
//    void updateTitleName() throws Exception {
//        // given
//        TitleName titleName = TitleName.builder()
//                .name("name")
//                .color("#000000")
//                .member(member)
//                .build();
//        titleNameRepository.save(titleName);
//
//        UpdateTitleNameReq req = UpdateTitleNameReq.builder()
//                .name("name!")
//                .color("#FFFFFF")
//                .build();
//        // when / then
//        mockMvc.perform(
//                        patch(TITLENAME_BASE_URL)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(req))
//                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
//                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
//                )
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    @DisplayName("PATCH /api/members/titleNames/my 요청 시 아무 데이터도 넘기지 않으면 400 에러를 반환한다.")
//    void updateTitleNameReturn400WithNoDatta() throws Exception {
//        // given
//        TitleName titleName = TitleName.builder()
//                .name("name")
//                .color("#000000")
//                .member(member)
//                .build();
//        titleNameRepository.save(titleName);
//
//        UpdateTitleNameReq req = UpdateTitleNameReq.builder()
//                .build();
//        // when / then
//        mockMvc.perform(
//                        patch(TITLENAME_BASE_URL)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(req))
//                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
//                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
//                )
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("code").value(ErrorCode.VALIDATION_ERROR.name()))
//                .andExpect(jsonPath("message").value("수정할 내용을 입력해주세요."));
//    }

    @Test
    @DisplayName("DELETE /api/members/titleNames/my 요청 시 칭호를 삭제한다.")
    void deleteTitleName() throws Exception {
        // given
        TitleName titleName = TitleName.builder()
                .name("name")
                .color("#000000")
                .member(member)
                .build();
        titleNameRepository.save(titleName);

        // when / then
        mockMvc.perform(
                        delete(TITLENAME_BASE_URL)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/members/titleNames/my 요청 시 칭호가 없으면 404 에러를 반환한다.")
    void deleteTitleNameReturn404WhenIsNoTitleName() throws Exception {
        // given

        // when / then
        mockMvc.perform(
                        delete(TITLENAME_BASE_URL)
                                .header(ACCESS_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getAccessToken())
                                .header(REFRESH_TOKEN_HEADER, BEARER_PREFIX + memberTokenDto.getRefreshToken())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value(ErrorCode.NOT_FOUND_TITLE_NAME.name()))
                .andExpect(jsonPath("message").value(ErrorCode.NOT_FOUND_TITLE_NAME.getMessage()));
    }
}