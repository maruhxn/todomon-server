package com.maruhxn.todomon.core.domain.item.application;

import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.dto.request.CreateItemRequest;
import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.core.domain.item.dto.request.UpdateItemRequest;
import com.maruhxn.todomon.core.domain.item.dto.response.InventoryItemDto;
import com.maruhxn.todomon.core.domain.item.dto.response.ItemDto;
import com.maruhxn.todomon.core.domain.item.implement.*;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.member.implement.MemberReader;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final MemberReader memberReader;

    private final InventoryItemRemover inventoryItemRemover;
    private final InventoryItemReader inventoryItemReader;
    private final InventoryItemCreator inventoryItemCreator;

    private final ItemReader itemReader;
    private final ItemApplier itemApplier;
    private final ItemRemover itemRemover;
    private final ItemCreator itemCreator;

    public void createItem(CreateItemRequest req) {
        Item item = CreateItemRequest.toEntity(req);
        itemCreator.create(item);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems() {
        return itemReader.findAllItems().stream()
                .map(ItemDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ItemDto getItemDto(Long itemId) {
        return ItemDto.from(itemReader.findItemById(itemId));
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDto> getInventoryItems(Long memberId) {
        return inventoryItemReader.findAllByMemberId(memberId).stream()
                .map(InventoryItemDto::from).toList();
    }

    public void updateItem(Long itemId, UpdateItemRequest req) {
        Item item = itemReader.findItemById(itemId);
        item.update(req);
    }

    public void deleteItem(Long itemId) {
        Item item = itemReader.findItemById(itemId);
        itemRemover.remove(item);
    }

    private boolean isPremiumAndMemberSubscription(Item item, Member findMember) {
        return item.getIsPremium() && !findMember.isSubscribed();
    }

    public void useInventoryItem(Long memberId, String itemName, ItemEffectRequest req) {
        Member member = memberReader.findById(memberId);
        InventoryItem inventoryItem = inventoryItemReader.findByMemberIdAndItemName(memberId, itemName);

        Item targetItem = inventoryItem.getItem();
        if (this.isPremiumAndMemberSubscription(targetItem, member)) {
            throw new ForbiddenException(ErrorCode.NOT_SUBSCRIPTION);
        }

        itemApplier.apply(targetItem, member, req);
        inventoryItemRemover.consume(inventoryItem);
    }


    public void postPurchase(Member member, Order order) {
        Item purchasedItem = order.getItem();

        if (this.isPremiumAndMemberSubscription(purchasedItem, member)) {
            throw new ForbiddenException(ErrorCode.NOT_SUBSCRIPTION);
        }

        switch (purchasedItem.getItemType()) {
            case CONSUMABLE ->
                    inventoryItemReader.findOptionalByMemberIdAndItemId(member.getId(), purchasedItem.getId())
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
                    itemApplier.apply(purchasedItem, member, null);
                }
            }
        }
    }
}
