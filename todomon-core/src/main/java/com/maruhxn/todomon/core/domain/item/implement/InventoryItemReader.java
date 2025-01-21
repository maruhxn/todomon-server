package com.maruhxn.todomon.core.domain.item.implement;

import com.maruhxn.todomon.core.domain.item.dao.InventoryItemRepository;
import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InventoryItemReader {

    private final InventoryItemRepository inventoryItemRepository;

    public List<InventoryItem> findAllByMemberId(Long memberId) {
        return inventoryItemRepository.findAllByMember_Id(memberId);
    }

    public InventoryItem findByMemberIdAndItemName(Long memberId, String itemName) {
        return inventoryItemRepository.findByMember_IdAndItem_Name(memberId, itemName)
                .orElseThrow(() -> new BadRequestException(ErrorCode.FORBIDDEN, itemName + "이(가) 없습니다."));
    }

    public Optional<InventoryItem> findOptionalByMemberIdAndItemId(Long memberId, Long itemId) {
        return inventoryItemRepository.findByMember_IdAndItem_Id(memberId, itemId);
    }
}
