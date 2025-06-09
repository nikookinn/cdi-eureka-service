package com.dafreurekadetails.dto.servicedto;

import java.util.List;

public record ServiceDetail(
        String serviceName,
        List<ServerInstance> servers
) {
}
