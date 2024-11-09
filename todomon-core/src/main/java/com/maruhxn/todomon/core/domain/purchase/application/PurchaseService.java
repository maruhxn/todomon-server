package com.maruhxn.todomon.core.domain.purchase.application;

import com.maruhxn.todomon.core.domain.item.application.ItemService;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.purchase.dao.OrderRepository;
import com.maruhxn.todomon.core.domain.purchase.dao.StarPointPaymentHistoryRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.domain.OrderStatus;
import com.maruhxn.todomon.core.domain.purchase.domain.StarPointPaymentHistory;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentRequest;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentRequest;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PurchaseStarPointItemRequest;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import com.maruhxn.todomon.infra.mail.dto.PaymentResourceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final StarPointPaymentHistoryRepository starPointPaymentHistoryRepository;
    private final ItemService itemService;

    private final PurchaseStrategyFactory purchaseStrategyFactory;

    public void preparePayment(Long memberId, PreparePaymentRequest req) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        Item findItem = itemService.getItem(req.getItemId());

        if (findItem.getIsPremium() && !findMember.isSubscribed()) {
            throw new ForbiddenException(ErrorCode.NOT_SUBSCRIPTION);
        }

        Order order = orderService.createOrder(findMember, findItem, req);// 주문 정보 생성

        // 아이템 재화 타입에 맞는 구매 전략 선택
        PurchaseStrategy purchaseStrategy = purchaseStrategyFactory.getStrategy(findItem.getMoneyType());

        try {
            purchaseStrategy.preValidate(findMember, findItem, req);
        } catch (Exception e) {
            log.error("사전 검증 실패 = {}", e);
            order.updateStatus(OrderStatus.FAILED);
            throw new BadRequestException(ErrorCode.PREPARE_PAYMENT_ERROR, e.getMessage());
        }

    }

    public PaymentResourceDTO verifyPayment(Long memberId, PaymentRequest req) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        Order findOrder = orderService.findByMerchant_uid(req.getMerchant_uid());

        PurchaseStrategy purchaseStrategy = purchaseStrategyFactory.getStrategy(findOrder.getItem().getMoneyType());

        try {
            purchaseStrategy.postValidate(findMember, findOrder, req);
        } catch (Exception e) {
            log.error("사후 검증 실패 = {}", e);
            findOrder.updateStatus(OrderStatus.FAILED);
            throw new BadRequestException(ErrorCode.POST_VALIDATE_PAYMENT_ERROR, e.getMessage());
        }

        findOrder.updateStatus(OrderStatus.OK); // order 상태 변경

        itemService.postPurchase(findMember, findOrder);

        log.info("결제 성공! 멤버 아이디: {}, 주문 아이디: {}", memberId, findOrder.getId());

        return PaymentResourceDTO.builder()
                .email(findMember.getEmail())
                .itemName(findOrder.getItem().getName())
                .quantity(findOrder.getQuantity())
                .totalPrice(findOrder.getTotalPrice())
                .build();
    }

    public void purchaseStarPointItem(Long memberId, PurchaseStarPointItemRequest req) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        StarPointPaymentHistory starPointPaymentHistory = PurchaseStarPointItemRequest.toEntity(findMember, req);
        starPointPaymentHistoryRepository.save(starPointPaymentHistory);
    }

    public PaymentResourceDTO cancelPayment(Long memberId, Long orderId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        Order findOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ORDER));

        PurchaseStrategy purchaseStrategy = purchaseStrategyFactory.getStrategy(findOrder.getItem().getMoneyType());

        try {
            purchaseStrategy.refund(findMember, findOrder);
        } catch (Exception e) {
            log.error("결제 취소 실패 = {}", e);
            findOrder.updateStatus(OrderStatus.CANCELED);
            throw new BadRequestException(ErrorCode.CANCEL_PAYMENT_ERROR, e.getMessage());
        }

        findOrder.updateStatus(OrderStatus.CANCELED);

        return PaymentResourceDTO.builder()
                .email(findMember.getEmail())
                .itemName(findOrder.getItem().getName())
                .quantity(findOrder.getQuantity())
                .totalPrice(findOrder.getTotalPrice())
                .build();
    }
}
