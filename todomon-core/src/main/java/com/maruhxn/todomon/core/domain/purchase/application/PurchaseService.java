package com.maruhxn.todomon.core.domain.purchase.application;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.implement.ItemReader;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentReq;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentReq;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PurchaseStarPointItemReq;
import com.maruhxn.todomon.core.domain.purchase.implement.OrderCreator;
import com.maruhxn.todomon.core.domain.purchase.implement.OrderReader;
import com.maruhxn.todomon.core.domain.purchase.implement.PurchaseManager;
import com.maruhxn.todomon.core.domain.purchase.implement.StarPointPaymentHistoryAppender;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.infra.mail.dto.PaymentResourceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private final MemberReader memberReader;
    private final ItemReader itemReader;
    private final StarPointPaymentHistoryAppender starPointPaymentHistoryCreator;
    private final OrderReader orderReader;
    private final OrderCreator orderCreator;

    private final PurchaseManager purchaseManager;

    public void requestToPurchaseStarPointItem(Long memberId, PurchaseStarPointItemReq req) {
        Member member = memberReader.findById(memberId);
        starPointPaymentHistoryCreator.create(req.toEntity(member));
    }

    private void checkIsPremiumItemAndMemberSubscription(Item item, Member member) {
        if (item.getIsPremium() && !member.isSubscribed()) {
            throw new ForbiddenException(ErrorCode.NOT_SUBSCRIPTION);
        }
    }

    public void preparePayment(Long memberId, PreparePaymentReq req) {
        Member member = memberReader.findById(memberId);
        Item item = itemReader.findItemById(req.getItemId());
        this.checkIsPremiumItemAndMemberSubscription(item, member);
        Order order = orderCreator.create(item, member, req);
        purchaseManager.preValidate(order, req);
    }

    private void validateMember(Long memberId, Member member) {
        if (!Objects.equals(member.getId(), memberId))
            throw new BadRequestException(ErrorCode.UNAUTHORIZED);
    }

    public PaymentResourceDTO verifyPayment(Long memberId, PaymentReq req) {
        Order order = orderReader.findByMerchantUid(req.getMerchant_uid());
        Member member = order.getMember();
        this.validateMember(memberId, member);
        Item item = order.getItem();
        this.checkIsPremiumItemAndMemberSubscription(item, member);

        purchaseManager.postValidate(order, req);
        purchaseManager.purchase(order);
        return this.createPaymentResourceDTO(member, order);
    }

    public PaymentResourceDTO cancelPayment(Long memberId, Long orderId) {
        Order order = orderReader.findById(orderId);
        Member member = order.getMember();
        this.validateMember(memberId, member);
        purchaseManager.refund(order);
        return this.createPaymentResourceDTO(member, order);
    }

    private PaymentResourceDTO createPaymentResourceDTO(Member member, Order order) {
        return PaymentResourceDTO.builder()
                .email(member.getEmail())
                .itemName(order.getItem().getName())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .build();
    }
}
