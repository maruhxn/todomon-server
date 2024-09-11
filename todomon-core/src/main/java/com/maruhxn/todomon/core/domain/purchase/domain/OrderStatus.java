package com.maruhxn.todomon.core.domain.purchase.domain;

public enum OrderStatus {
    REQUEST_PAYMENT, // 결제 요청
    CANCELED, // 주문 취소
    FAILED, // 결제시간 초과 등으로 주문 실패
    OK // 결제 완료
}
