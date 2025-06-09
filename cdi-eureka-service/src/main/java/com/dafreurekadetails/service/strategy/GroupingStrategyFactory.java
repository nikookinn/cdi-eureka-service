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
    public GroupingStrategy resolve(String groupBy){
        return strategies.getOrDefault(groupBy,strategies.get("servers"));
    }
}
