package com.maruhxn.todomon.core.domain.purchase.application;

import com.maruhxn.todomon.core.domain.item.application.ItemService;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.implement.ItemReader;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.purchase.application.strategy.PurchaseStrategy;
import com.maruhxn.todomon.core.domain.purchase.dao.OrderRepository;
import com.maruhxn.todomon.core.domain.purchase.dao.StarPointPaymentHistoryRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.domain.OrderStatus;
import com.maruhxn.todomon.core.domain.purchase.domain.StarPointPaymentHistory;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentReq;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentReq;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PurchaseStarPointItemReq;
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
    private final StarPointPaymentHistoryRepository starPointPaymentHistoryRepository;
    private final ItemService itemService;
    private final ItemReader itemReader;

    private final PurchaseStrategyFactory purchaseStrategyFactory;

    public void preparePayment(Long memberId, PreparePaymentReq req) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        Item findItem = itemReader.findItemById(req.getItemId());

        if (findItem.getIsPremium() && !findMember.isSubscribed()) {
            throw new ForbiddenException(ErrorCode.NOT_SUBSCRIPTION);
        }

        Order order = Order.of(findItem, findMember, req.getQuantity(), req.getMerchant_uid());
        orderRepository.save(order);

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

    public PaymentResourceDTO verifyPayment(Long memberId, PaymentReq req) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        Order findOrder = orderRepository.findByMerchantUid(req.getMerchant_uid())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ORDER));

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

        return this.createPaymentResourceDTO(findMember, findOrder);
    }

    public void requestToPurchaseStarpointItem(Long memberId, PurchaseStarPointItemReq req) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        StarPointPaymentHistory starPointPaymentHistory = req.toEntity(findMember);
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

        return this.createPaymentResourceDTO(findMember, findOrder);
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
