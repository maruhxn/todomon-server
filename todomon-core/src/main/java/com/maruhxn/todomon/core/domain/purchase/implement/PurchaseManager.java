package com.maruhxn.todomon.core.domain.purchase.implement;

import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.implement.InventoryItemCreator;
import com.maruhxn.todomon.core.domain.item.implement.InventoryItemReader;
import com.maruhxn.todomon.core.domain.item.implement.ItemApplier;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.domain.purchase.domain.OrderStatus;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PaymentReq;
import com.maruhxn.todomon.core.domain.purchase.dto.request.PreparePaymentReq;
import com.maruhxn.todomon.core.domain.purchase.implement.strategy.PurchaseStrategy;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseManager {

    private final PurchaseStrategyFactory purchaseStrategyFactory;
    private final InventoryItemReader inventoryItemReader;
    private final InventoryItemCreator inventoryItemCreator;
    private final ItemApplier itemApplier;

    public void preValidate(Order order, PreparePaymentReq req) {
        Item item = order.getItem();
        // 아이템 재화 타입에 맞는 구매 전략 선택
        PurchaseStrategy purchaseStrategy = purchaseStrategyFactory.getStrategy(item.getMoneyType());

        Member member = order.getMember();
        try {
            purchaseStrategy.preValidate(order, req);
        } catch (Exception e) {
            log.error("사전 검증 실패 = {}", e);
            order.updateStatus(OrderStatus.FAILED);
            throw new BadRequestException(ErrorCode.PREPARE_PAYMENT_ERROR, e.getMessage());
        }

        log.info("결제 요청! 멤버 아이디: {}, 주문 아이디: {}", member.getId(), order.getId());
    }

    public void postValidate(Order order, PaymentReq req) {
        PurchaseStrategy purchaseStrategy = purchaseStrategyFactory.getStrategy(order.getItem().getMoneyType());

        Member member = order.getMember();
        try {
            purchaseStrategy.postValidate(order, req);
        } catch (Exception e) {
            log.error("사후 검증 실패 = {}", e);
            order.updateStatus(OrderStatus.FAILED);
            throw new BadRequestException(ErrorCode.POST_VALIDATE_PAYMENT_ERROR, e.getMessage());
        }

        log.info("결제 성공! 멤버 아이디: {}, 주문 아이디: {}", member.getId(), order.getId());

        order.updateStatus(OrderStatus.OK); // order 상태 변경
    }

    public void purchase(Order order) {
        Member member = order.getMember();
        Item item = order.getItem();

        switch (item.getItemType()) {
            case CONSUMABLE -> inventoryItemReader.findOptionalByMemberIdAndItemId(member.getId(), item.getId())
                    .ifPresentOrElse(
                            existingItem ->
                                    // 인벤토리에 해당 아이템이 있으면 수량 수정
                                    existingItem.addQuantity(order.getQuantity())
                            ,
                            () -> {
                                // 없다면 생성
                                InventoryItem newInventoryItem = InventoryItem.of(member, order);
                                member.addItemToInventory(newInventoryItem);
                                inventoryItemCreator.create(newInventoryItem);
                            }
                    );
            case IMMEDIATE_EFFECT -> {
                // 즉시 효과 적용
                for (int i = 0; i < order.getQuantity(); i++) {
                    itemApplier.apply(item, member, null);
                }
            }
        }
    }

    public void refund(Order order) {
        PurchaseStrategy purchaseStrategy = purchaseStrategyFactory.getStrategy(order.getItem().getMoneyType());

        try {
            purchaseStrategy.refund(order);
        } catch (Exception e) {
            log.error("결제 취소 실패 = {}", e);
            order.updateStatus(OrderStatus.CANCELED);
            throw new BadRequestException(ErrorCode.CANCEL_PAYMENT_ERROR, e.getMessage());
        }

        order.updateStatus(OrderStatus.CANCELED);

        log.info("결제 취소! 멤버 아이디: {}, 주문 아이디: {}", order.getMember().getId(), order.getId());
    }

}
