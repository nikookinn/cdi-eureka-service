package com.dafreurekadetails.service.strategy;

import com.dafreurekadetails.dto.GroupedResult;

public interface GroupingStrategy {
    GroupedResult group(String eurekaServerUrl);
}
