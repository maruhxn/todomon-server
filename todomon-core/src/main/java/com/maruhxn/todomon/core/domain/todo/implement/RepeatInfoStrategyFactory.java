package com.maruhxn.todomon.core.domain.todo.implement;

import com.maruhxn.todomon.core.domain.todo.implement.strategy.RepeatInfoStrategy;
import com.maruhxn.todomon.core.domain.todo.domain.Frequency;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RepeatInfoStrategyFactory {

    private final Map<Frequency, RepeatInfoStrategy> strategies;

    public RepeatInfoStrategyFactory(List<RepeatInfoStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(RepeatInfoStrategy::getFrequency, strategy -> strategy));
    }

    public RepeatInfoStrategy getStrategy(Frequency frequency) {
        return strategies.get(frequency);
    }
}
