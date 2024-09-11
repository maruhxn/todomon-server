package com.maruhxn.todomon.core.domain.item.dto.request;

import com.maruhxn.todomon.core.domain.item.domain.Item;
import com.maruhxn.todomon.core.domain.item.domain.ItemType;
import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateItemRequest {

    @Size(min = 1, max = 30, message = "아이템명은 1 ~ 30글자이어야 합니다.")
    private String name;

    @Size(min = 1, max = 255, message = "아이템 설명은 1 ~ 255글자이어야 합니다.")
    private String description;

    private ItemType itemType;

    private MoneyType moneyType;

    private String effectName;

    @Min(value = 0, message = "가격은 양수만 가능합니다.")
    private Long price;

    @Builder
    public CreateItemRequest(String name, String description, ItemType itemType, MoneyType moneyType, String effectName, Long price) {
        this.name = name;
        this.description = description;
        this.itemType = itemType;
        this.moneyType = moneyType;
        this.effectName = effectName;
        this.price = price;
    }

    public static Item toEntity(CreateItemRequest req) {
        return Item.builder()
                .name(req.getName())
                .description(req.getDescription())
                .itemType(req.getItemType())
                .effectName(req.getEffectName())
                .price(req.getPrice())
                .moneyType(req.getMoneyType())
                .build();
    }
}
