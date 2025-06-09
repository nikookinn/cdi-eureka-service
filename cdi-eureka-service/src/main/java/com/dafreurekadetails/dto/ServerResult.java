package com.dafreurekadetails.dto;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.serverdto.ServerGroup;

import java.util.List;

public record ServerResult(
        List<ServerGroup> servers) implements GroupedResult {
}
