package com.maruhxn.todomon.core.domain.item.dto.response;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.ItemType;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemDto {

    private Long id;
    private String name;
    private String description;
    private Boolean isPremium;
    private ItemType itemType;
    private MoneyType moneyType;
    private Long price;
    private Boolean isAvailable;

    @Builder
    public ItemDto(Long id, String name, String description, Boolean isPremium, ItemType itemType, MoneyType moneyType, Long price, Boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isPremium = isPremium;
        this.itemType = itemType;
        this.moneyType = moneyType;
        this.price = price;
        this.isAvailable = isAvailable;
    }

    public static ItemDto from(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .isPremium(item.getIsPremium())
                .price(item.getPrice())
                .itemType(item.getItemType())
                .moneyType(item.getMoneyType())
                .isAvailable(item.getIsAvailable())
                .build();
    }
}