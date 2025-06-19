package com.dafreurekadetails.dto.servicedto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Wrapper for server instance")
public record ServerInstance(
        @Schema(description = "Details of the server instance")
        ServerInstanceDetail server
) {
}
