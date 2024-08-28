package com.maruhxn.todomon.domain.purchase.dao;

import com.maruhxn.todomon.domain.purchase.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
