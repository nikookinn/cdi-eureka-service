package com.dafreurekadetails.dto;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.servicedto.ServiceGroup;

import java.util.List;

public record ServiceResult(
        List<ServiceGroup> services) implements GroupedResult {
}
