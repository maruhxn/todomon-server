package com.maruhxn.todomon.core.domain.purchase.implement;

import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.implement.InventoryItemReader;
import com.maruhxn.todomon.core.domain.item.implement.InventoryItemWriter;
import com.maruhxn.todomon.core.domain.item.implement.ItemApplier;
import com.maruhxn.todomon.core.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseManager {

    private final InventoryItemReader inventoryItemReader;
    private final InventoryItemWriter inventoryItemWriter;
    private final ItemApplier itemApplier;

    public void purchase(Member member, Item item, Long quantity) {
        switch (item.getItemType()) {
            case CONSUMABLE -> inventoryItemReader.findOptionalByMemberIdAndItemId(member.getId(), item.getId())
                    .ifPresentOrElse(
                            existingItem ->
                                    // 인벤토리에 해당 아이템이 있으면 수량 수정
                                    existingItem.addQuantity(quantity)
                            ,
                            () -> {
                                // 없다면 생성
                                InventoryItem newInventoryItem = InventoryItem.of(member, item, quantity);
                                member.addItemToInventory(newInventoryItem);
                                inventoryItemWriter.create(newInventoryItem);
                            }
                    );
            case IMMEDIATE_EFFECT -> {
                // 즉시 효과 적용
                for (int i = 0; i < quantity; i++) {
                    itemApplier.apply(item, member, null);
                }
            }
        }
    }
}
