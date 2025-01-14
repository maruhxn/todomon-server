package com.maruhxn.todomon.core.domain.item.implement;

import com.maruhxn.todomon.core.domain.item.dao.ItemRepository;
import com.maruhxn.todomon.core.domain.item.domain.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemWriter {

    private final ItemRepository itemRepository;

    public void create(Item item) {
        itemRepository.save(item);
    }

    public void remove(Item item) {
        itemRepository.delete(item);
    }
}
