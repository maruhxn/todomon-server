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
    EMPTY_REFRESH_TOKEN("Refresh Token이 비어있습니다."),
    OVER_FOOD_CNT("소지한 먹이 수보다 요청된 먹이 수가 더 많습니다."),
    NO_SPACE_PET_HOUSE("펫 하우스의 공간이 부족합니다."),


    /* UNAUTHORIZED 401 */
    UNAUTHORIZED("로그인이 필요한 서비스입니다."),
    INVALID_TOKEN("유효하지 않은 토큰입니다."),

    /* FORBIDDEN 403 */
    FORBIDDEN("권한이 없습니다."),
    NOT_SUBSCRIPTION("구독하지 않은 이용자입니다. 해당 서비스를 이용하려면 유료 플랜을 구독해주세요."),

    /* NOT FOUND 404 */
    NOT_FOUND_RESOURCE("요청하신 자원이 존재하지 않습니다."),
    NOT_FOUND_MEMBER("멤버 정보가 존재하지 않습니다."),
    NOT_FOUND_REFRESH_TOKEN("Refresh Token 정보가 존재하지 않습니다."),
    NOT_FOUND_TODO("할 일 정보가 존재하지 않습니다."),
    NOT_FOUND_PET("펫 정보가 존재하지 않습니다."),

    /* UNPROCESSABLE CONTENT 422 */
    EXISTING_RESOURCE("이미 존재하는 리소스입니다."),

    /* INTERNAL SERVER ERROR  500 */
    INTERNAL_ERROR("서버 오류입니다.");

    private final String message;
}
