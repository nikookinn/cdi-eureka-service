package com.dafreurekadetails.dto;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.serverdto.ServerGroup;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
@Schema(description = "Server-based grouping result")
public record ServerResult(
        @Schema(description = "List of server groups")
        List<ServerGroup> servers) implements GroupedResult {
}
