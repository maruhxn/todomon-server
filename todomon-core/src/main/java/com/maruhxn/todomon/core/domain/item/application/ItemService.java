package com.maruhxn.todomon.core.domain.item.application;

import com.maruhxn.todomon.core.domain.item.dao.InventoryItemRepository;
import com.maruhxn.todomon.core.domain.item.dao.ItemRepository;
import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.item_effect.ItemEffect;
import com.maruhxn.todomon.core.domain.item.dto.request.CreateItemRequest;
import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.core.domain.item.dto.request.UpdateItemRequest;
import com.maruhxn.todomon.core.domain.item.dto.response.InventoryItemDto;
import com.maruhxn.todomon.core.domain.item.dto.response.ItemDto;
import com.maruhxn.todomon.core.domain.member.dao.MemberRepository;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.ForbiddenException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final ApplicationContext applicationContext;
    private final InventoryItemRepository inventoryItemRepository;

    public void createItem(CreateItemRequest req) {
        itemRepository.save(CreateItemRequest.toEntity(req));
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(ItemDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ItemDto getItemDto(Long itemId) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ITEM));

        return ItemDto.from(findItem);
    }

    @Transactional(readOnly = true)
    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ITEM));
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDto> getInventoryItems(Long memberId) {
        return inventoryItemRepository.findAllByMember_Id(memberId).stream()
                .map(InventoryItemDto::from).toList();
    }

    public void updateItem(Long itemId, UpdateItemRequest req) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ITEM));

        findItem.update(req);
    }

    public void deleteItem(Long itemId) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ITEM));

        itemRepository.delete(findItem);
    }

    public void useInventoryItem(Long memberId, String itemName, ItemEffectRequest req) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MEMBER));

        InventoryItem findInventoryItem = inventoryItemRepository.findByMember_IdAndItem_Name(memberId, itemName)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.FORBIDDEN, itemName + "이(가) 없습니다."));

        Item targetItem = findInventoryItem.getItem();

        if (this.isPremiumAndMemberSubscription(targetItem, findMember)) {
            throw new ForbiddenException(ErrorCode.NOT_SUBSCRIPTION);
        }

        this.applyItemEffect(findMember, targetItem, req);

        this.consumeItem(findInventoryItem);
    }

    private boolean isPremiumAndMemberSubscription(Item item, Member findMember) {
        return item.getIsPremium() && !findMember.isSubscribed();
    }

    private void applyItemEffect(Member member, Item item, ItemEffectRequest request) {
        String effectName = item.getEffectName();
        ItemEffect itemEffect = (ItemEffect) applicationContext.getBean(effectName);
        itemEffect.applyEffect(member, request);
    }

    private void consumeItem(InventoryItem inventoryItem) {
        if (inventoryItem.getQuantity() <= 1) inventoryItemRepository.delete(inventoryItem);
        else inventoryItem.decreaseQuantity();
    }

    public void postPurchase(Member member, Order order) {
        Item purchasedItem = order.getItem();

        if (this.isPremiumAndMemberSubscription(purchasedItem, member)) {
            throw new ForbiddenException(ErrorCode.NOT_SUBSCRIPTION);
        }

        switch (purchasedItem.getItemType()) {
            case CONSUMABLE -> inventoryItemRepository
                    .findByMember_IdAndItem_Id(member.getId(), purchasedItem.getId())
                    .ifPresentOrElse(
                            existingItem -> {
                                // 인벤토리에 해당 아이템이 있으면 수량 수정
                                existingItem.addQuantity(order.getQuantity());
                            },
                            () -> {
                                // 없다면 생성
                                InventoryItem newInventoryItem = InventoryItem.of(member, order);
                                member.addItemToInventory(newInventoryItem);
                                inventoryItemRepository.save(newInventoryItem);
                            }
                    );

            case IMMEDIATE_EFFECT -> {
                // 즉시 효과 적용
                for (int i = 0; i < order.getQuantity(); i++) {
                    this.applyItemEffect(member, purchasedItem, null);
                }
            }
        }
    }
}
