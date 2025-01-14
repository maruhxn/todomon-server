package com.maruhxn.todomon.core.domain.item.api;

import com.maruhxn.todomon.core.domain.item.application.ItemService;
import com.maruhxn.todomon.core.domain.item.dto.request.CreateItemRequest;
import com.maruhxn.todomon.core.domain.item.dto.request.ItemEffectReq;
import com.maruhxn.todomon.core.domain.item.dto.request.UpdateItemRequest;
import com.maruhxn.todomon.core.domain.item.dto.response.InventoryItemDto;
import com.maruhxn.todomon.core.domain.item.dto.response.ItemDto;
import com.maruhxn.todomon.core.global.auth.model.TodomonOAuth2User;
import com.maruhxn.todomon.core.global.common.dto.response.BaseResponse;
import com.maruhxn.todomon.core.global.common.dto.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public BaseResponse createItem(@RequestBody CreateItemRequest req) {
        itemService.createItem(req);
        return new BaseResponse("아이템 생성 성공");
    }

    @GetMapping
    public DataResponse<List<ItemDto>> getAllItems() {
        return DataResponse.of("아이템 전체 조회 성공", itemService.getAllItems());
    }

    @GetMapping("/inventory")
    public DataResponse<List<InventoryItemDto>> getInventoryItems(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User
    ) {
        return DataResponse.of("인벤토리 조회 성공", itemService.getInventoryItems(todomonOAuth2User.getId()));
    }

    @GetMapping("/{itemId}")
    public DataResponse<ItemDto> getItem(@PathVariable Long itemId) {
        return DataResponse.of("아이템 조회 성공", itemService.getItemDto(itemId));
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateItem(@PathVariable Long itemId, @RequestBody UpdateItemRequest req) {
        itemService.updateItem(itemId, req);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
    }


    @PostMapping("/use")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void useInventoryItem(
            @AuthenticationPrincipal TodomonOAuth2User todomonOAuth2User,
            @RequestParam String itemName,
            @RequestBody ItemEffectReq req) {
        itemService.useInventoryItem(todomonOAuth2User.getId(), itemName, req);
    }
}
