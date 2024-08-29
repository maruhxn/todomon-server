package com.maruhxn.todomon.domain.item.application;

import com.maruhxn.todomon.domain.item.dao.ItemRepository;
import com.maruhxn.todomon.domain.item.domain.Item;
import com.maruhxn.todomon.domain.item.dto.request.CreateItemRequest;
import com.maruhxn.todomon.domain.item.dto.request.UpdateItemRequest;
import com.maruhxn.todomon.domain.item.dto.response.ItemDto;
import com.maruhxn.todomon.global.error.ErrorCode;
import com.maruhxn.todomon.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;


    public void createItem(CreateItemRequest req) {
        Item item = CreateItemRequest.toEntity(req);
        itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream().map(ItemDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ItemDto getItem(Long itemId) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ITEM));

        return ItemDto.from(findItem);
    }

    public void updateItem(Long itemId, UpdateItemRequest req) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ITEM));

        findItem.update(req);
    }

    public void deleteItem(Long itemId) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ITEM));

        itemRepository.delete(findItem);
    }
}
