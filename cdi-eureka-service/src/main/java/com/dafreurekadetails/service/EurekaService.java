package com.dafreurekadetails.service;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.service.strategy.GroupingStrategy;
import com.dafreurekadetails.service.strategy.GroupingStrategyFactory;
import org.springframework.stereotype.Service;
/**
 * EurekaService delegates the grouping logic to the appropriate {@link GroupingStrategy}
 * implementation based on the given grouping key.
 */
@Service
public class EurekaService {
    private final GroupingStrategyFactory strategyFactory;

    public EurekaService(GroupingStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }
    /**
     * Executes grouping logic for Eureka apps using the strategy resolved by the given key.
     *
     * @param groupBy    the key to determine which strategy to use
     * @param eurekaURL  the Eureka server URL to fetch data from
     * @return the grouped result based on the strategy logic
     */
    public GroupedResult group(String groupBy, String eurekaURL){
        GroupingStrategy strategy = strategyFactory.resolve(groupBy);
        return strategy.group(eurekaURL);
    }
}
