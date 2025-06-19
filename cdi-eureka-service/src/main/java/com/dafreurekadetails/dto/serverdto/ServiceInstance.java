package com.dafreurekadetails.dto.serverdto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Service instances")
public record ServiceInstance(
        @Schema(description = "Details of the service instance")
        ServiceInstanceDetail service
) {
}
