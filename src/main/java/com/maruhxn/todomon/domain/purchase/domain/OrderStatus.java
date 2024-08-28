package com.maruhxn.todomon.domain.purchase.domain;

public enum OrderStatus {
    REQUEST_PAYMENT, // 결제 요청
    CANCEL, // 주문 취소
    FAIL, // 결제시간 초과 등으로 주문 실패
    OK // 결제 완료
}
