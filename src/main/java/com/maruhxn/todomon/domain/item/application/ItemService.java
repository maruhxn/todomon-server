package com.maruhxn.todomon.domain.item.application;

import com.maruhxn.todomon.domain.item.dao.InventoryItemRepository;
import com.maruhxn.todomon.domain.item.dao.ItemRepository;
import com.maruhxn.todomon.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.domain.item.domain.Item;
import com.maruhxn.todomon.domain.item.domain.item_effect.ItemEffect;
import com.maruhxn.todomon.domain.item.dto.request.CreateItemRequest;
import com.maruhxn.todomon.domain.item.dto.request.ItemEffectRequest;
import com.maruhxn.todomon.domain.item.dto.request.UpdateItemRequest;
import com.maruhxn.todomon.domain.item.dto.response.ItemDto;
import com.maruhxn.todomon.domain.member.domain.Member;
import com.maruhxn.todomon.domain.purchase.domain.Order;
import com.maruhxn.todomon.global.auth.checker.AuthChecker;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
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
    private final ApplicationContext applicationContext;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemService inventoryItemService;

    private final AuthChecker authChecker;


    public void createItem(CreateItemRequest req) {
        Item item = CreateItemRequest.toEntity(req);
        itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream().map(ItemDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ItemDto getItemDto(Long itemId) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ITEM));

        return ItemDto.from(findItem);
    }

    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ITEM));
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

    public void postPurchase(Member member, Order order) {
        Item purchasedItem = order.getItem();

        authChecker.isPremiumButNotSubscribing(member, purchasedItem);

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
                applyItemEffect(member, purchasedItem, null);
            }
        }
    }

    private void applyItemEffect(Member member, Item item, ItemEffectRequest request) {
        String effectName = item.getEffectName();
        ItemEffect itemEffect = (ItemEffect) applicationContext.getBean(effectName);
        itemEffect.applyEffect(member, request);
    }

    public void useInventoryItem(Member member, String itemName, ItemEffectRequest req) {
        InventoryItem findInventoryItem = inventoryItemService.getInventoryItem(member.getId(), itemName);

        authChecker.isPremiumButNotSubscribing(member, findInventoryItem.getItem());

        applyItemEffect(member, findInventoryItem.getItem(), req);

        inventoryItemService.consumeItem(findInventoryItem);
    }

}
