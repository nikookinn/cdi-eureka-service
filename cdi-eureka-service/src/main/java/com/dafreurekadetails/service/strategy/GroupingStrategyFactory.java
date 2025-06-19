package com.dafreurekadetails.service.strategy;

import com.dafreurekadetails.service.strategy.GroupingStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;
/**
 * GroupingStrategyFactory holds a map of available {@link GroupingStrategy} implementations
 * and resolves the appropriate one based on the provided group key.
 */
@Component
public class GroupingStrategyFactory {
    private final Map<String, GroupingStrategy> strategies;

    public GroupingStrategyFactory(Map<String, GroupingStrategy> strategies) {
        this.strategies = strategies;
    }
    /**
     * Resolves the appropriate grouping strategy based on the groupBy key.
     *
     * @param groupBy the key indicating which strategy to use
     * @return the corresponding {@link GroupingStrategy}, or a default if key is not found
     */
    public GroupingStrategy resolve(String groupBy) {
        if (groupBy == null || !strategies.containsKey(groupBy)) {
            return strategies.get("servers");
        }
        return strategies.get(groupBy);
    }
}
