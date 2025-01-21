package com.maruhxn.todomon.core.domain.payment.domain;

public enum OrderStatus {
    REQUESTED, // 주문 요청
    PAID, // 결제 완료
    OK, // 주문 완료
    FAILED, // 주문 실패
    CANCELED, // 주문 취소
}
