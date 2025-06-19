package com.dafreurekadetails.dto.servicedto;

import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed server instance information")
public record ServerInstanceDetail(
        @Schema(description = "Hostname of the server")
        String hostname,
        @Schema(description = "Base instance detail")
        BaseInstanceDetail instanceDetail
) {
}
