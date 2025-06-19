package com.dafreurekadetails.controller;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.request.EurekaQueryRequest;
import com.dafreurekadetails.dto.response.EurekaQueryResponse;
import com.dafreurekadetails.service.EurekaQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 * REST controller that exposes endpoints for querying applications from a Eureka server.
 * Accepts grouping instructions and a Eureka URL, and returns grouped data.
 */
@RestController
@RequestMapping("/cdi-eureka-service/v1/eureka")
public class EurekaQueryController {
    private final EurekaQueryService eurekaQueryService;
    public EurekaQueryController(EurekaQueryService eurekaQueryService) {
        this.eurekaQueryService = eurekaQueryService;
    }

    /**
     * Receives client requests to fetch and group apps from Eureka.
     *
     * @param request contains the groupBy key and the Eureka server URL
     * @return response entity containing the grouped result and metadata
     */
    @PostMapping("/apps")
    public ResponseEntity<EurekaQueryResponse<? extends GroupedResult>> getApps(@Valid @RequestBody EurekaQueryRequest request) {
        EurekaQueryResponse<? extends GroupedResult> response =
                eurekaQueryService.handleQuery(request.groupBy(), request.eurekaServerURL());

        return ResponseEntity.status(response.httpStatusCode()).body(response);

    }
}
