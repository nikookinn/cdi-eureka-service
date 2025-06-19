package com.dafreurekadetails.dto.response;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.ServerResult;
import com.dafreurekadetails.dto.ServiceResult;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A generic response wrapper used to return data from Eureka query operations.
 *
 * @param returnCode     standardized return code from {@link ReturnCode}
 * @param message        human-readable message about the result
 * @param httpStatusCode HTTP status code representing the result (e.g., 200, 400)
 * @param transactionID  unique ID for tracing the request lifecycle
 * @param elapsedTime    time taken to process the query in milliseconds
 * @param data           the actual response data, grouped by strategy
 * @param <T>            the type of the grouped result extending {@link GroupedResult}
 */
@Schema(name = "EurekaQueryResponse", description = "Generic response wrapper for Eureka query results.")
public record EurekaQueryResponse<T extends GroupedResult>(
        @Schema(description = "Standardized return code from the operation.", example = "SUCCESS")
        String returnCode,
        @Schema(description = "Human-readable message about the result.", example = "Operation completed successfully.")
        String message,
        @Schema(description = "HTTP status code representing the result.", example = "200")
        int httpStatusCode,
        @Schema(description = "Unique transaction ID for tracing the request.", example = "f6e6f1f8-379e-45d3-b7cb-0cc9d823a7f6")
        String transactionID,
        @Schema(description = "Time taken to process the query in milliseconds.", example = "123.0")
        double elapsedTime,
        @JsonUnwrapped
        @Schema(description = "Polymorphic response data. The actual JSON response does not contain a `data` field.  " +
                "Instead, fields from either ServerResult (`servers`) or ServiceResult (`services`) appear directly at the root level.",
                oneOf = { ServerResult.class, ServiceResult.class })
        T data
) {
    /**
     * Factory method to create a response object from a {@link ReturnCode} and other parameters.
     *
     * @param rc         the return code object
     * @param customMessage optional custom message
     * @param txId       transaction ID for tracing
     * @param elapsed    elapsed time in ms
     * @param data       result data
     * @return a new {@link EurekaQueryResponse} instance
     */
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

