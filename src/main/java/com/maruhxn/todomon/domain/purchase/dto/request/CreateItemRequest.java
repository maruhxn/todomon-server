package com.maruhxn.todomon.domain.purchase.dto.request;

import com.maruhxn.todomon.domain.purchase.domain.Item;
import com.maruhxn.todomon.domain.purchase.domain.ItemType;
import com.maruhxn.todomon.domain.purchase.domain.MoneyType;
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

    @Min(value = 0, message = "가격은 양수만 가능합니다.")
    private Long price;

    @Builder
    public CreateItemRequest(String name, String description, ItemType itemType, MoneyType moneyType, Long price) {
        this.name = name;
        this.description = description;
        this.itemType = itemType;
        this.moneyType = moneyType;
        this.price = price;
    }

    public static Item toEntity(CreateItemRequest req) {
        return Item.builder()
                .name(req.getName())
                .description(req.getDescription())
                .itemType(req.getItemType())
                .price(req.getPrice())
                .moneyType(req.getMoneyType())
                .build();
    }
}
