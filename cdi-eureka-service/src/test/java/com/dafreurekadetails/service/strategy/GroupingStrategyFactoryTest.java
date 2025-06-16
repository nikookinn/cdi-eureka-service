package com.dafreurekadetails.service.strategy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

public class GroupingStrategyFactoryTest {
    @Mock
    private GroupingStrategy serviceStrategy;
    @Mock
    private GroupingStrategy serverStrategy;
    @InjectMocks
    private GroupingStrategyFactory groupingStrategyFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Map<String,GroupingStrategy> strategies = Map.of(
                "servers",serverStrategy,
                "services",serviceStrategy
        );
        groupingStrategyFactory = new GroupingStrategyFactory(strategies);
    }
    @Test
    public void resolve_shouldReturnCorrectStrategy_WhenValidGroupBy(){
        String groupBy = "services";
        GroupingStrategy strategy = groupingStrategyFactory.resolve(groupBy);
        Assertions.assertEquals(serviceStrategy,strategy);
    }
    @Test
    public void resolve_shouldReturnDefaultStrategy_WhenInvalidGroupBy(){
        String groupBy = "abc";
        GroupingStrategy strategy = groupingStrategyFactory.resolve(groupBy);
        Assertions.assertEquals(serverStrategy,strategy);
    }
    @Test
    public void resolve_shouldReturnDefaultStrategy_WhenNullGroupBy() {
        GroupingStrategy strategy = groupingStrategyFactory.resolve(null);
        Assertions.assertEquals(serverStrategy, strategy);
    }
}
