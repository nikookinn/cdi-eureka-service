package com.dafreurekadetails.service;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.ServerResult;
import com.dafreurekadetails.dto.ServiceResult;
import com.dafreurekadetails.service.strategy.GroupingStrategy;
import com.dafreurekadetails.service.strategy.GroupingStrategyFactory;
import com.dafreurekadetails.service.strategy.ServerGroupingStrategy;
import com.dafreurekadetails.service.strategy.ServiceGroupingStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EurekaServiceTest {

    @Mock
    private GroupingStrategyFactory strategyFactory;

    @Mock
    private ServiceGroupingStrategy serviceGroupingStrategy;

    @Mock
    private ServerGroupingStrategy serverGroupingStrategy;
    @Mock
    private ServiceResult serviceResult;

    @Mock
    private ServerResult serverResult;

    @InjectMocks
    private EurekaService eurekaService;

    @Test
    void group_ShouldUseCorrectStrategyAndReturnResult() {
        String groupBy = "services";
        String eurekaUrl = "http://localhost:8080/eureka";


        when(strategyFactory.resolve(groupBy)).thenReturn(serviceGroupingStrategy);
        when(serviceGroupingStrategy.group(eurekaUrl)).thenReturn(serviceResult);

        ServiceResult result = (ServiceResult) eurekaService.group(groupBy, eurekaUrl);

        assertEquals(serviceResult, result);
        verify(strategyFactory).resolve(groupBy);
        verify(serviceGroupingStrategy).group(eurekaUrl);
    }

    @Test
    void group_ShouldUseDefaultStrategy_WhenGroupByIsUnknown() {
        String groupBy = "unknown";
        String eurekaUrl = "http://localhost:8080/eureka";


        when(strategyFactory.resolve(groupBy)).thenReturn(serverGroupingStrategy);
        when(serverGroupingStrategy.group(eurekaUrl)).thenReturn(serverResult);

        ServerResult result = (ServerResult) eurekaService.group(groupBy, eurekaUrl);

        assertEquals(serverResult, result);
        verify(strategyFactory).resolve(groupBy);
        verify(serverGroupingStrategy).group(eurekaUrl);
    }

    @Test
    void group_ShouldUseDefaultStrategy_WhenGroupByIsNull() {
        String eurekaUrl = "http://localhost:8080/eureka";

        when(strategyFactory.resolve(null)).thenReturn(serverGroupingStrategy);
        when(serverGroupingStrategy.group(eurekaUrl)).thenReturn(serverResult);

        ServerResult result = (ServerResult) eurekaService.group(null, eurekaUrl);

        assertEquals(serverResult, result);
        verify(strategyFactory).resolve(null);
        verify(serverGroupingStrategy).group(eurekaUrl);
    }
}



