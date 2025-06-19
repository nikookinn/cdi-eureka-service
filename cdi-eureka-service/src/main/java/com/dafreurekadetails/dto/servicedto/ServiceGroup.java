package com.dafreurekadetails.dto.servicedto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Wrapper for service group")
public record ServiceGroup(
        @Schema(description = "Service detail")
        ServiceDetail service
) {
}
