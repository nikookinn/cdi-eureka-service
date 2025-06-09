package com.dafreurekadetails.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EurekaQueryRequest(
        @NotBlank(message = "Eureka server URL must not be blank.")
        @Pattern(
                regexp = "^https?://[\\w.-]+(:\\d+)?/eureka/?$",
                message = "Eureka server URL must start with http:// or https://, contain a valid host, and end with /eureka"
        )
        String eurekaServerURL,
        @NotBlank(message = "GroupBy parameter is required.")
        @Pattern(regexp = "^(servers|services)$", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "groupBy must be either 'servers' or 'services'")
        String groupBy
) {
}
