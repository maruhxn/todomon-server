package com.maruhxn.todomon.core.domain.purchase.dao;

import com.maruhxn.todomon.core.domain.purchase.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"item", "member"})
    Optional<Order> findByMerchantUid(String merchantUid);

    @Query("SELECT o FROM Order o JOIN FETCH Member m WHERE o.id = :id")
    Optional<Order> findByIdWithMember(Long id);

    List<Order> findAllByMember_IdOrderByUpdatedAtDesc(Long memberId);
}
