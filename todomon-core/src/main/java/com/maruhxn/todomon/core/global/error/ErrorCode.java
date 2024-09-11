package com.maruhxn.todomon.core.global.error;

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
    ALREADY_RECEIVED("이미 ⭐️을 받았습니다."),
    EMPTY_FILE("파일은 비어있을 수 없습니다."),
    ALREADY_SENT_STAR("⭐️은 하루에 한번만 보낼 수 있습니다."),
    PREPARE_PAYMENT_ERROR("결제 정보 사전 검증 에러"),
    POST_VALIDATE_PAYMENT_ERROR("결제 정보 사후 검증 에러"),
    NOT_ENOUGH_STAR_POINT("⭐️이 부족합니다."),
    INVALID_PAYMENT_AMOUNT_ERROR("거래 금액이 일치하지 않습니다."),
    CANCEL_PAYMENT_ERROR("결제 취소 중 에러"),

    /* UNAUTHORIZED 401 */
    UNAUTHORIZED("로그인이 필요한 서비스입니다."),
    INVALID_TOKEN("유효하지 않은 토큰입니다."),

    /* FORBIDDEN 403 */
    FORBIDDEN("권한이 없습니다."),
    NOT_SUBSCRIPTION("구독하지 않은 이용자입니다. 해당 서비스를 이용하려면 유료 플랜을 구독해주세요."),
    NOT_ACCEPTED_FOLLOW("팔로우하고 있지 않은 사용자입니다."),

    /* NOT FOUND 404 */
    NOT_FOUND_RESOURCE("요청하신 자원이 존재하지 않습니다."),
    NOT_FOUND_MEMBER("멤버 정보가 존재하지 않습니다."),
    NOT_FOUND_REFRESH_TOKEN("Refresh Token 정보가 존재하지 않습니다."),
    NOT_FOUND_TODO("할 일 정보가 존재하지 않습니다."),
    NOT_FOUND_PET("펫 정보가 존재하지 않습니다."),
    NOT_FOUND_FOLLOW("팔로우 정보가 존재하지 않습니다."),
    NOT_FOUND_STAR_TRANSACTION("STAR 발신 내역이 없습니다."),
    NOT_FOUND_TITLE_NAME("유저 칭호를 찾을 수 없습니다."),
    NOT_FOUND_FILE("파일을 찾을 수 없습니다."),
    NOT_FOUND_ITEM("상품 정보를 찾을 수 없습니다."),
    NOT_FOUND_ORDER("주문 정보를 찾을 수 없습니다."),
    NOT_FOUND_STAR_POINT_PAYMENT_HISTORY("⭐️ 아이템 구매 기록을 찾을 수 없습니다."),
    NOT_FOUND_PAYMENT("결제 정보를 찾을 수 없습니다."),

    /* UNPROCESSABLE CONTENT 422 */
    EXISTING_RESOURCE("이미 존재하는 리소스입니다."),
    EXISTING_MEMBER("이미 존재하는 멤버입니다."),
    EXISTING_TITLENAME("이미 칭호가 존재합니다."),

    /* INTERNAL SERVER ERROR  500 */
    INTERNAL_ERROR("서버 오류입니다."),
    S3_UPLOAD_ERROR("S3 파일 업로드 중 오류가 발생했습니다.");

    private final String message;
}
