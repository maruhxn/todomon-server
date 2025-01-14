package com.maruhxn.todomon.core.domain.purchase.implement.strategy;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.domain.PaymentStatus;
import com.maruhxn.todomon.core.domain.purchase.domain.StarPointPaymentHistory;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentReq;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentReq;
import com.maruhxn.todomon.core.domain.purchase.implement.StarPointPaymentHistoryReader;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StarPointPurchaseStrategy implements PurchaseStrategy {

    private final StarPointPaymentHistoryReader starPointPaymentHistoryReader;

    private boolean isNotStarPointItem(Item item) {
        return !item.getMoneyType().equals(MoneyType.STARPOINT);
    }

    @Override
    public void preValidate(Order order, PreparePaymentReq req) throws Exception {
        Item item = order.getItem();
        if (this.isNotStarPointItem(item)) throw new BadRequestException(ErrorCode.BAD_REQUEST);

        long totalPrice = item.getPrice() * req.getQuantity();

        Member member = order.getMember();
        if (member.getStarPoint() < totalPrice) {
            throw new BadRequestException(ErrorCode.NOT_ENOUGH_STAR_POINT);
        }
    }

    @Override
    public void postValidate(Order order, PaymentReq req) throws Exception {
        if (this.isNotStarPointItem(order.getItem()))
            throw new BadRequestException(ErrorCode.BAD_REQUEST);

        Member member = order.getMember();

        StarPointPaymentHistory paymentHistory = starPointPaymentHistoryReader
                .findByMemberIdAndMerchantUid(member.getId(), order.getMerchantUid());

        if (this.isNotMatch(order.getTotalPrice(), paymentHistory.getAmount())) {
            paymentHistory.updateStatus(PaymentStatus.FAILED);
            throw new BadRequestException(ErrorCode.INVALID_PAYMENT_AMOUNT_ERROR);
        }

        paymentHistory.updateStatus(PaymentStatus.OK);

        member.subtractStarPoint(order.getTotalPrice());
    }

    private boolean isNotMatch(Long orderAmount, Long historyAmount) {
        return !Objects.equals(orderAmount, historyAmount);
    }

    @Override
    public void refund(Order order) throws Exception {
        Member member = order.getMember();

        starPointPaymentHistoryReader
                .findByMemberIdAndMerchantUid(member.getId(), order.getMerchantUid())
                .updateStatus(PaymentStatus.REFUNDED);

        member.addStarPoint(order.getTotalPrice());
    }
}
