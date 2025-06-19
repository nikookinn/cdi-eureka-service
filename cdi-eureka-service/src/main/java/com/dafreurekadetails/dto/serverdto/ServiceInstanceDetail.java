package com.dafreurekadetails.dto.serverdto;

import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed service instance including base instance info")
public record ServiceInstanceDetail(
        @Schema(description = "Name of the service")
        String serviceName,
        @Schema(description = "Instance detail of the service")
        @JsonUnwrapped
        BaseInstanceDetail instanceDetail
) {
}
