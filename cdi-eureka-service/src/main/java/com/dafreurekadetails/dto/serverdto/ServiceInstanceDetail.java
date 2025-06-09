package com.dafreurekadetails.dto.serverdto;

import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record ServiceInstanceDetail(
        String serviceName,
        @JsonUnwrapped
        BaseInstanceDetail instanceDetail
) {
}
