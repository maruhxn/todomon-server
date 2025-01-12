package com.maruhxn.todomon.core.domain.purchase.application;

import com.maruhxn.todomon.core.domain.item.domain.MoneyType;
import com.maruhxn.todomon.core.domain.purchase.application.strategy.PurchaseStrategy;
import com.maruhxn.todomon.core.domain.purchase.application.strategy.RealMoneyPurchaseStrategy;
import com.maruhxn.todomon.core.domain.purchase.application.strategy.StarPointPurchaseStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PurchaseStrategyFactory {

    private final Map<MoneyType, PurchaseStrategy> strategyMap;

    // 모든 PurchaseStrategy 구현체들을 주입받아 Map에 저장
    public PurchaseStrategyFactory(List<PurchaseStrategy> strategies) {
        strategyMap = strategies.stream()
                .collect(Collectors.toMap(this::getStrategyType, strategy -> strategy));
    }

    // MoneyType 맞는 전략 반환
    public PurchaseStrategy getStrategy(MoneyType moneyType) {
        return strategyMap.get(moneyType);
    }

    // 각 전략 구현체가 처리할 MoneyType을 매핑하는 메서드
    private MoneyType getStrategyType(PurchaseStrategy strategy) {
        if (strategy instanceof RealMoneyPurchaseStrategy) {
            return MoneyType.REAL_MONEY;
        } else if (strategy instanceof StarPointPurchaseStrategy) {
            return MoneyType.STARPOINT;
        } else {
            throw new IllegalArgumentException("지원되지 않는 전략입니다.");
        }
    }
}

