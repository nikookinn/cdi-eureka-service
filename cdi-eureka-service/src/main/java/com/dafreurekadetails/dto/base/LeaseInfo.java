package com.dafreurekadetails.dto.base;

public record LeaseInfo(
        int renewalIntervalInSecs,
        int durationInSecs,
        long registrationTimestamp,
        long lastRenewalTimestamp,
        long evictionTimestamp,
        long serviceUpTimestamp
) {
}
