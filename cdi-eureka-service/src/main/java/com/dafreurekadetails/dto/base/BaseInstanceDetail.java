package com.dafreurekadetails.dto.base;

import com.fasterxml.jackson.annotation.JsonUnwrapped;


public record BaseInstanceDetail(
        String ipAddr,
        int port,
        int securePort,
        String url,
        String homePageUrl,
        String statusPageUrl,
        String status,
        long lastUpdatedTimestamp,
        long lastDirtyTimestamp,
        boolean isCoordinatingDiscoveryServer,
        Metadata metadataMap,
        LeaseInfo leaseInfo
) {
}
