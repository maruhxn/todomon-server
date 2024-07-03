package com.maruhxn.todomon.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    /* BAD REQUEST 400 */
    BAD_REQUEST("잘못된 접근입니다."),
    VALIDATION_ERROR("올바르지 않은 입력입니다."),
    PATH_ERROR("올바르지 않은 경로입니다."),

    /* UNAUTHORIZED 401 */
    UNAUTHORIZED("로그인이 필요한 서비스입니다."),

    /* FORBIDDEN 403 */
    FORBIDDEN("권한이 없습니다."),

    /* NOT FOUND 404 */
    NOT_FOUND_RESOURCE("요청하신 자원이 존재하지 않습니다."),

    /* UNPROCESSABLE CONTENT 422 */
    EXISTING_RESOURCE("이미 존재하는 리소스입니다."),

    /* INTERNAL SERVER ERROR  500 */
    INTERNAL_ERROR("서버 오류입니다.");

    private final String message;
}
