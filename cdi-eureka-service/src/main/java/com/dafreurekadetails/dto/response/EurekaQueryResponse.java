package com.dafreurekadetails.dto.response;

import com.dafreurekadetails.dto.GroupedResult;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record EurekaQueryResponse<T extends GroupedResult>(
        String returnCode,
        String message,
        int httpStatusCode,
        String transactionID,
        double elapsedTime,
        @JsonUnwrapped
        T data
) {
    public static <T extends GroupedResult> EurekaQueryResponse<T> from(ReturnCode rc,
                                                                        String customMessage,
                                                                        String txId,
                                                                        double elapsed,
                                                                        T data) {
        return new EurekaQueryResponse<>(
                rc.getCode(),
                customMessage != null ? customMessage : rc.getMessage(),
                rc.status(),
                txId,
                elapsed,
                data
        );
    }
}

