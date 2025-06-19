package com.dafreurekadetails.dto;

import com.dafreurekadetails.dto.servicedto.ServiceGroup;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
@Schema(description = "Service-based grouping result")
public record ServiceResult(
        @Schema(description = "List of service groups")
        List<ServiceGroup> services) implements GroupedResult {
}
