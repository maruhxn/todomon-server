package com.maruhxn.todomon.domain.purchase.dao;

import com.maruhxn.todomon.domain.purchase.domain.StarPointPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StarPointPaymentHistoryRepository extends JpaRepository<StarPointPaymentHistory, Long> {

    Optional<StarPointPaymentHistory> findByMember_IdAndMerchantUid(Long memberId, String merchantUid);
}
