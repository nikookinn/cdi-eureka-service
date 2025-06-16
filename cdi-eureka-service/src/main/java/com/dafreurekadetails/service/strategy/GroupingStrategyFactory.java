package com.dafreurekadetails.service.strategy;

import com.dafreurekadetails.service.strategy.GroupingStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class GroupingStrategyFactory {
    private final Map<String, GroupingStrategy> strategies;

    public GroupingStrategyFactory(Map<String, GroupingStrategy> strategies) {
        this.strategies = strategies;
    }
    public GroupingStrategy resolve(String groupBy) {
        if (groupBy == null || !strategies.containsKey(groupBy)) {
            return strategies.get("servers");
        }
        return strategies.get(groupBy);
    }
}
