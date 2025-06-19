package com.dafreurekadetails.dto.servicedto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
@Schema(description = "Service detail containing servers")
public record ServiceDetail(
        @Schema(description = "Name of the service")
        String serviceName,
        @Schema(description = "List of servers providing the service")
        List<ServerInstance> servers
) {
}
