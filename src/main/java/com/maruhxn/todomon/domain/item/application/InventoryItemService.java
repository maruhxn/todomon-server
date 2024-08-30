package com.maruhxn.todomon.domain.item.application;

import com.maruhxn.todomon.domain.item.dao.InventoryItemRepository;
import com.maruhxn.todomon.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryItem getInventoryItem(Long memberId, String itemName) {
        return inventoryItemRepository.findByMember_IdAndItem_Name(memberId, itemName)
                .orElseThrow(() -> new ForbiddenException(ErrorCode.FORBIDDEN, itemName + "이(가) 없습니다."));
    }

    public void consumeItem(InventoryItem inventoryItem) {
        if (inventoryItem.getQuantity() > 1) {
            inventoryItem.consume();
        } else {
            inventoryItemRepository.delete(inventoryItem);
        }
    }
}
