package com.dafreurekadetails.dto.serverdto;

import java.util.List;

public record ServerDetail(
        String hostName,
        List<ServiceInstance> services) {
}
