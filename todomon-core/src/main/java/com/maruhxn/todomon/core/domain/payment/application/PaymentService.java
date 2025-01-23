package com.maruhxn.todomon.core.domain.payment.application;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.implement.ItemReader;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.payment.domain.Order;
import com.maruhxn.todomon.core.domain.payment.domain.OrderStatus;
import com.maruhxn.todomon.core.domain.payment.domain.PaymentStatus;
import com.maruhxn.todomon.core.domain.payment.domain.TodomonPayment;
import com.maruhxn.todomon.core.domain.payment.dto.request.PreparePaymentReq;
import com.maruhxn.todomon.core.domain.payment.dto.request.WebhookPayload;
import com.maruhxn.todomon.core.domain.payment.implement.*;
import com.maruhxn.todomon.core.domain.purchase.implement.PurchaseManager;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.core.global.error.exception.InternalServerException;
import com.maruhxn.todomon.infra.mail.dto.PaymentResourceDTO;
import com.maruhxn.todomon.infra.payment.PaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

import static com.maruhxn.todomon.core.domain.payment.domain.OrderStatus.OK;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MemberReader memberReader;
    private final ItemReader itemReader;
    private final OrderReader orderReader;
    private final OrderWriter orderWriter;
    private final RollbackManager rollbackManager;
    private final PaymentProvider paymentProvider;
    private final PurchaseManager purchaseManager;
    private final RefundProvider refundProvider;
    private final PaidOrderProducer paidOrderProducer;


    private void checkIsPremiumItemAndMemberSubscription(Item item, Member member) {
        if (item.getIsPremium() && !member.isSubscribed()) {
            throw new ForbiddenException(ErrorCode.NOT_SUBSCRIPTION);
        } else if (item.getName().equals("유료 플랜 구독권") && member.isSubscribed()) {
            throw new BadRequestException(ErrorCode.ALREADY_SUBSCRIPTION);
        }
    }

    @Transactional
    public void preparePayment(Long memberId, PreparePaymentReq req) {
        log.info("결제 요청 === 멤버 아이디: {}, 주문 아이디: {}", memberId, req.getMerchant_uid());
        Member member = memberReader.findById(memberId);
        Item item = itemReader.findItemById(req.getItemId());
        this.checkIsPremiumItemAndMemberSubscription(item, member);

        try {
            paymentProvider.prepare(req.getMerchant_uid(), req.getAmount());
            orderWriter.create(item, member, req);
            log.info("사전 검증 성공 === 멤버 아이디: {}, 주문 아이디: {}", memberId, req.getMerchant_uid());
        } catch (Exception e) {
            log.error("사전 검증 실패 === 멤버 아이디: {}, 주문 아이디: {}, 이유: {}", memberId, req.getMerchant_uid(), e.getMessage());
            rollbackManager.prepareStageRollback(memberId, req);
            log.error("사전 검증 실패 로직 수행 성공 === 멤버 아이디: {}, 주문 아이디: {}", memberId, req.getMerchant_uid());
            throw new InternalServerException(ErrorCode.PREPARE_PAYMENT_ERROR, e.getMessage());
        }
    }

    private void validateMember(Long memberId, Member member) {
        if (!Objects.equals(member.getId(), memberId))
            throw new BadRequestException(ErrorCode.UNAUTHORIZED);
    }

    @Transactional
    public void completePayment(WebhookPayload req) {
        log.info("웹훅 수신 === {}", req);
        if (!"paid".equals(req.getStatus())) return;

        log.info("결제 완료 이벤트 처리 요청 === 주문 아이디: {}, 결제 아이디: {}", req.getMerchant_uid(), req.getImp_uid());

        Order order = orderReader.findByMerchantUid(req.getMerchant_uid());
        Member member = order.getMember();
        Item item = order.getItem();
        this.checkIsPremiumItemAndMemberSubscription(item, member);
        TodomonPayment todomonPayment = TodomonPayment.of(order, req.getImp_uid());
        order.setPayment(todomonPayment);

        try {
            paymentProvider.complete(req.getImp_uid(), BigDecimal.valueOf(order.getTotalPrice()));
            order.updateStatus(OrderStatus.PAID);
            log.info("사후 검증 성공 === 멤버 아이디: {}, 주문 아이디: {}", order.getMember().getId(), order.getMerchantUid());
            paidOrderProducer.send(member.getId(), req.getMerchant_uid());
        } catch (Exception e) {
            log.error("사후 검증 실패 === 멤버 아이디: {}, 주문 아이디: {}, 이유: {}", member.getId(), req.getMerchant_uid(), e.getMessage());
            rollbackManager.completeStageRollback(todomonPayment, req);
            log.error("사후 검증 실패 로직 수행 성공 === 멤버 아이디: {}, 주문 아이디: {}", member.getId(), req.getMerchant_uid());
            throw new InternalServerException(ErrorCode.POST_VALIDATE_PAYMENT_ERROR, e.getMessage());
        }
    }

    @Transactional
    public PaymentResourceDTO purchaseItem(Long memberId, String merchantUid) {
        log.info("프리미엄 아이템 구매 처리 요청 수신 === 유저 아이디: {}, 주문 아이디: {}", memberId, merchantUid);
        Order order = orderReader.findByMerchantUid(merchantUid);

        try {
            purchaseManager.purchase(order.getMember(), order.getItem(), order.getQuantity());
            order.updateStatus(OK);
            log.info("프리미엄 아이템 구매 성공 === 멤버 아이디: {}, 주문 아이디: {}", memberId, merchantUid);
            return this.createPaymentResourceDTO(order);
        } catch (Exception e) {
            log.error("프리미엄 아이템 구매 실패 === 멤버 아이디: {}, 주문 아이디: {}, 이유: {}", memberId, merchantUid, e.getMessage());
            rollbackManager.purchaseStageRollback(merchantUid);
            log.error("프리미엄 아이템 구매 실패 로직 수행 성공 === 멤버 아이디: {}, 주문 아이디: {}", memberId, merchantUid);
            throw new InternalServerException(ErrorCode.PURCHASE_ERROR, e.getMessage());
        }
    }

    @Transactional
    public PaymentResourceDTO cancelPayment(Long memberId, String merchantUid) {
        log.info("환불 처리 요청 수신 === 유저 아이디: {}, 주문 아이디: {}", memberId, merchantUid);
        Order order = orderReader.findByMerchantUid(merchantUid);
        Member member = order.getMember();
        this.validateMember(memberId, member);

        if (order.getItem().getName().equals("유료 플랜 구독권")) {
            log.info("구독 취소 === 유저 아이디: {}, 주문 아이디: {}", memberId, merchantUid);
            member.updateIsSubscribed(false);
        }

        refundProvider.refund(order);
        TodomonPayment todomonPayment = order.getPayment();
        order.updateStatus(OrderStatus.CANCELED);
        todomonPayment.updateStatus(PaymentStatus.REFUNDED);
        log.info("환불 성공 === 멤버 아이디: {}, 주문 아이디: {}", order.getMember().getId(), order.getMerchantUid());
        return this.createPaymentResourceDTO(order);
    }

    private PaymentResourceDTO createPaymentResourceDTO(Order order) {
        return PaymentResourceDTO.builder()
                .email(order.getMember().getEmail())
                .itemName(order.getItem().getName())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .build();
    }
}
