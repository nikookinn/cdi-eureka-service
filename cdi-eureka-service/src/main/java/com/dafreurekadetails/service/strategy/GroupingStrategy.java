package com.dafreurekadetails.service.strategy;

import com.dafreurekadetails.dto.GroupedResult;
/**
 * GroupingStrategy defines the contract for grouping applications fetched from Eureka.
 * Implementations can group data based on services, servers
 */
public interface GroupingStrategy {

    /**
     * Performs grouping logic on the data fetched from the given Eureka server URL.
     *
     * @param eurekaServerUrl the Eureka URL to fetch data from
     * @return grouped data encapsulated in a {@link GroupedResult}
     */
    GroupedResult group(String eurekaServerUrl);
}
