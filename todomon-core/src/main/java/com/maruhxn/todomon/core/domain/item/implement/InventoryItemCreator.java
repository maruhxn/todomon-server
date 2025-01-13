package com.maruhxn.todomon.core.domain.item.implement;

import com.maruhxn.todomon.core.domain.item.dao.InventoryItemRepository;
import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryItemCreator {

    private final InventoryItemRepository inventoryItemRepository;

    public void create(InventoryItem inventoryItem) {
        inventoryItemRepository.save(inventoryItem);
    }
}
