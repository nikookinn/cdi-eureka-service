package com.dafreurekadetails.dto.base;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of a single instance registered in Eureka")
public record BaseInstanceDetail(
        @Schema(description = "IP address of the instance")
        String ipAddr,
        @Schema(description = "Port number the instance is running on")
        int port,
        @Schema(description = "Secure port number the instance is running on")
        int securePort,
        @Schema(description = "Health check URL")
        String url,
        @Schema(description = "Home page URL of the instance")
        String homePageUrl,
        @Schema(description = "Status page URL of the instance")
        String statusPageUrl,
        @Schema(description = "Status of the instance (e.g. UP, DOWN)")
        String status,
        @Schema(description = "Timestamp of last update")
        long lastUpdatedTimestamp,
        @Schema(description = "Timestamp of last dirty state")
        long lastDirtyTimestamp,
        @Schema(description = "Indicates if this is a coordinating discovery server")
        boolean isCoordinatingDiscoveryServer,
        @Schema(description = "Metadata information of the instance")
        Metadata metadataMap,
        @Schema(description = "Lease information of the instance")
        LeaseInfo leaseInfo
) {
}
