package com.dafreurekadetails.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Metadata(
        String version,
        String region,
        String zone,
        String instanceType,
        String buildNumber
) {
}
