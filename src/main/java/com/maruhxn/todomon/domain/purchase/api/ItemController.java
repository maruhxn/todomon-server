package com.maruhxn.todomon.domain.purchase.api;

import com.maruhxn.todomon.domain.purchase.application.ItemService;
import com.maruhxn.todomon.domain.purchase.dto.request.CreateItemRequest;
import com.maruhxn.todomon.domain.purchase.dto.request.UpdateItemRequest;
import com.maruhxn.todomon.domain.purchase.dto.response.ItemDto;
import com.maruhxn.todomon.global.common.dto.response.BaseResponse;
import com.maruhxn.todomon.global.common.dto.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
        List<ItemDto> items = itemService.getAllItems();
        return DataResponse.of("아이템 전체 조회 성공", items);
    }

    @GetMapping("/{itemId}")
    public DataResponse<ItemDto> getItem(@PathVariable Long itemId) {
        ItemDto item = itemService.getItem(itemId);
        return DataResponse.of("아이템 조회 성공", item);
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
}
