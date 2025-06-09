package com.dafreurekadetails.controller;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.request.EurekaQueryRequest;
import com.dafreurekadetails.dto.response.EurekaQueryResponse;
import com.dafreurekadetails.dto.response.ReturnCode;
import com.dafreurekadetails.service.EurekaQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cdi-eureka-service/v1/eureka")
public class EurekaQueryController {
    private final EurekaQueryService eurekaQueryService;

    public EurekaQueryController(EurekaQueryService eurekaQueryService) {
        this.eurekaQueryService = eurekaQueryService;
    }

    @GetMapping("/apps")
    public ResponseEntity<EurekaQueryResponse<? extends GroupedResult>> getApps(@Valid @RequestBody EurekaQueryRequest request) {
        EurekaQueryResponse<? extends GroupedResult> response =
                eurekaQueryService.handleQuery(request.groupBy(), request.eurekaServerURL());

        return ResponseEntity.status(ReturnCode.SUCCESS.status()).body(response);

    }
}
