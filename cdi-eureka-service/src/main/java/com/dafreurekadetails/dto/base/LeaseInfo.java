package com.dafreurekadetails.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;

public record LeaseInfo(
        @Schema(description = "Time in seconds between client renewals")
        int renewalIntervalInSecs,
        @Schema(description = "Total duration of lease in seconds")
        int durationInSecs,
        @Schema(description = "Timestamp when instance was registered")
        long registrationTimestamp,
        @Schema(description = "Last time the instance renewed its lease")
        long lastRenewalTimestamp,
        @Schema(description = "Timestamp when the instance was evicted")
        long evictionTimestamp,
        @Schema(description = "When the service was marked as UP")
        long serviceUpTimestamp
) {
}
