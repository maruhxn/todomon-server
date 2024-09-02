package com.maruhxn.todomon.domain.purchase.application;

import com.maruhxn.todomon.domain.item.domain.Item;
import com.maruhxn.todomon.domain.item.domain.MoneyType;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.purchase.dao.StarPointPaymentHistoryRepository;
import com.maruhxn.todomon.domain.purchase.domain.*;
import com.maruhxn.todomon.domain.purchase.dto.request.PaymentRequest;
import com.maruhxn.todomon.domain.purchase.dto.request.PreparePaymentRequest;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.BadRequestException;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StarPointPurchaseStrategy implements PurchaseStrategy {

    private final StarPointPaymentHistoryRepository starPointPaymentHistoryRepository;

    @Override
    public void preValidate(Member member, Item item, PreparePaymentRequest req) throws Exception {
        if (isNotStarPointItem(item)) throw new BadRequestException(ErrorCode.BAD_REQUEST);

        long totalPrice = item.getPrice() * req.getQuantity();

        if (member.getStarPoint() < totalPrice) {
            throw new BadRequestException(ErrorCode.NOT_ENOUGH_STAR_POINT);
        }
    }

    private static boolean isNotStarPointItem(Item item) {
        return !item.getMoneyType().equals(MoneyType.STARPOINT);
    }

    @Override
    public void postValidate(Member member, Order order, PaymentRequest req) throws Exception {
        if (isNotStarPointItem(order.getItem())) throw new BadRequestException(ErrorCode.BAD_REQUEST);

        StarPointPaymentHistory findPaymentHistory = starPointPaymentHistoryRepository
                .findByMember_IdAndMerchantUid(member.getId(), req.getMerchant_uid())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_STAR_POINT_PAYMENT_HISTORY));

        if (!Objects.equals(findPaymentHistory.getAmount(), order.getTotalPrice())) {
            findPaymentHistory.updateStatus(PaymentStatus.FAILED);
            throw new BadRequestException(ErrorCode.INVALID_PAYMENT_AMOUNT_ERROR);
        }

        member.subtractStarPoint(order.getTotalPrice());
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
