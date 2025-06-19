package com.dafreurekadetails.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Metadata object for additional instance info")
@JsonIgnoreProperties(ignoreUnknown = true)
public record Metadata(
        @Schema(description = "Version of the instance")
        String version,
        @Schema(description = "Region of the instance")
        String region,
        @Schema(description = "Zone where instance is running")
        String zone,
        @Schema(description = "Instance type")
        String instanceType,
        @Schema(description = "Build number")
        String buildNumber
) {
}
