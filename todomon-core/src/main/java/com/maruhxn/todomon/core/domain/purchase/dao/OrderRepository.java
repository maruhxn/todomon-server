package com.maruhxn.todomon.core.domain.purchase.dao;

import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"item"})
    Optional<Order> findByMerchantUid(String merchantUid);

    List<Order> findAllByMember_IdOrderByUpdatedAtDesc(Long memberId);
}
