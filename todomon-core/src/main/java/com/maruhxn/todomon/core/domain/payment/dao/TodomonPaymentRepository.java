package com.maruhxn.todomon.core.domain.payment.dao;

import com.maruhxn.todomon.core.domain.payment.domain.TodomonPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodomonPaymentRepository extends JpaRepository<TodomonPayment, String> {

}
