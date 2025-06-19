package com.dafreurekadetails.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result interface for grouped query responses")
public sealed interface GroupedResult permits ServerResult, ServiceResult {
}
