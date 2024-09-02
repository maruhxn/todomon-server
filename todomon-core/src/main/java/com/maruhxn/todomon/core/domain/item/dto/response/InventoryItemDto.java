package com.maruhxn.todomon.core.domain.item.dto.response;

import com.maruhxn.todomon.core.domain.item.domain.InventoryItem;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean isPremium;
    private Long quantity;

    @Builder
    public InventoryItemDto(Long id, String name, String description, Boolean isPremium, Long quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isPremium = isPremium;
        this.quantity = quantity;
    }

    public static InventoryItemDto from(InventoryItem inventoryItem) {
        return InventoryItemDto.builder()
                .id(inventoryItem.getId())
                .name(inventoryItem.getItem().getName())
                .description(inventoryItem.getItem().getDescription())
                .isPremium(inventoryItem.getItem().getIsPremium())
                .quantity(inventoryItem.getQuantity())
                .build();
    }
}
