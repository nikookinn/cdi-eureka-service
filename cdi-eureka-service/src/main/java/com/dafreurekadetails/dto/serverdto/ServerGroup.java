package com.dafreurekadetails.dto.serverdto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Wrapper for server detail grouping")
public record ServerGroup(
        @Schema(description = "Server detail")
        ServerDetail server
) {
}
