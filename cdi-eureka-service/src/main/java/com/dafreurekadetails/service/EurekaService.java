package com.dafreurekadetails.service;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.service.strategy.GroupingStrategy;
import com.dafreurekadetails.service.strategy.GroupingStrategyFactory;
import org.springframework.stereotype.Service;

@Service
public class EurekaService {
    private final GroupingStrategyFactory strategyFactory;

    public EurekaService(GroupingStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public GroupedResult group(String groupBy, String eurekaURL){
        GroupingStrategy strategy = strategyFactory.resolve(groupBy);
        return strategy.group(eurekaURL);
    }
}
