package com.maruhxn.todomon.core.domain.item.implement;

import com.maruhxn.todomon.core.domain.item.dao.InventoryItemRepository;
import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryItemRemover {

    private final InventoryItemRepository inventoryItemRepository;

    public void consume(InventoryItem inventoryItem) {
        if (inventoryItem.getQuantity() <= 1) inventoryItemRepository.delete(inventoryItem);
        else inventoryItem.decreaseQuantity();
    }
}
