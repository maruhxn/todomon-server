package com.maruhxn.todomon.core.domain.purchase.application.strategy;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.purchase.dao.StarPointPaymentHistoryRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.domain.PaymentStatus;
import com.maruhxn.todomon.core.domain.purchase.domain.StarPointPaymentHistory;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentReq;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentReq;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StarPointPurchaseStrategy implements PurchaseStrategy {

    private final StarPointPaymentHistoryRepository starPointPaymentHistoryRepository;

    private boolean isNotStarPointItem(Item item) {
        return !item.getMoneyType().equals(MoneyType.STARPOINT);
    }

    @Override
    public void preValidate(Member member, Item item, PreparePaymentReq req) throws Exception {
        if (this.isNotStarPointItem(item)) throw new BadRequestException(ErrorCode.BAD_REQUEST);

        long totalPrice = item.getPrice() * req.getQuantity();

        if (member.getStarPoint() < totalPrice) {
            throw new BadRequestException(ErrorCode.NOT_ENOUGH_STAR_POINT);
        }
    }

    @Override
    public void postValidate(Member member, Order order, PaymentReq req) throws Exception {
        if (this.isNotStarPointItem(order.getItem()))
            throw new BadRequestException(ErrorCode.BAD_REQUEST);

        StarPointPaymentHistory findPaymentHistory = starPointPaymentHistoryRepository
                .findByMember_IdAndMerchantUid(member.getId(), req.getMerchant_uid())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_STAR_POINT_PAYMENT_HISTORY));

        if (this.isNotMatch(order.getTotalPrice(), findPaymentHistory.getAmount())) {
            findPaymentHistory.updateStatus(PaymentStatus.FAILED);
            throw new BadRequestException(ErrorCode.INVALID_PAYMENT_AMOUNT_ERROR);
        }

        member.subtractStarPoint(order.getTotalPrice());
    }

    private boolean isNotMatch(Long orderAmount, Long historyAmount) {
        return !Objects.equals(orderAmount, historyAmount);
    }

    @Override
    public void refund(Member member, Order order) throws Exception {
        StarPointPaymentHistory findPaymentHistory = starPointPaymentHistoryRepository
                .findByMember_IdAndMerchantUid(member.getId(), order.getMerchantUid())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_STAR_POINT_PAYMENT_HISTORY));

        member.addStarPoint(order.getTotalPrice());

        findPaymentHistory.updateStatus(PaymentStatus.REFUNDED);

    }
}
