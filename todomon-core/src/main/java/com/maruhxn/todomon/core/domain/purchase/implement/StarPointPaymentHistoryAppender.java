package com.maruhxn.todomon.core.domain.purchase.implement;

import com.maruhxn.todomon.core.domain.purchase.dao.StarPointPaymentHistoryRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.StarPointPaymentHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StarPointPaymentHistoryAppender {

    private final StarPointPaymentHistoryRepository repository;

    public void create(StarPointPaymentHistory starPointPaymentHistory) {
        repository.save(starPointPaymentHistory);
    }
}
