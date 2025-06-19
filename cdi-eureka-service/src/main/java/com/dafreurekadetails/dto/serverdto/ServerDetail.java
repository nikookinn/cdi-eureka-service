package com.dafreurekadetails.dto.serverdto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
@Schema(description = "Details of a service hosted on a server")
public record ServerDetail(
        @Schema(description = "Host name of the server")
        String hostName,
        @Schema(description = "List of services running on the server")
        List<ServiceInstance> services) {
}
