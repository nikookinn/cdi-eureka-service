package com.dafreurekadetails.dto.servicedto;

import com.dafreurekadetails.dto.base.BaseInstanceDetail;

public record ServerInstanceDetail(
        String hostname,
        BaseInstanceDetail instanceDetail
) {
}
