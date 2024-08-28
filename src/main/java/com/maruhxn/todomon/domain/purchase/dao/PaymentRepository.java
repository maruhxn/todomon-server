package com.maruhxn.todomon.domain.purchase.dao;

import com.maruhxn.todomon.domain.purchase.domain.TodomonPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<TodomonPayment, Long> {
}
