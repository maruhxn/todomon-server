package com.maruhxn.todomon.core.domain.purchase.implement;

import com.maruhxn.todomon.core.domain.purchase.dao.StarPointPaymentHistoryRepository;
import com.maruhxn.todomon.core.domain.purchase.domain.StarPointPaymentHistory;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StarPointPaymentHistoryReader {

    private final StarPointPaymentHistoryRepository starPointPaymentHistoryRepository;

    public StarPointPaymentHistory findById(Long historyId) {
        return starPointPaymentHistoryRepository
                .findById(historyId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_STAR_POINT_PAYMENT_HISTORY));
    }

}
