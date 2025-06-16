package com.dafreurekadetails.controller;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.ServerResult;
import com.dafreurekadetails.dto.ServiceResult;
import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.dafreurekadetails.dto.base.LeaseInfo;
import com.dafreurekadetails.dto.base.Metadata;
import com.dafreurekadetails.dto.request.EurekaQueryRequest;
import com.dafreurekadetails.dto.response.EurekaQueryResponse;
import com.dafreurekadetails.dto.response.ReturnCode;
import com.dafreurekadetails.dto.serverdto.ServerDetail;
import com.dafreurekadetails.dto.serverdto.ServerGroup;
import com.dafreurekadetails.dto.serverdto.ServiceInstance;
import com.dafreurekadetails.dto.serverdto.ServiceInstanceDetail;
import com.dafreurekadetails.dto.servicedto.ServerInstance;
import com.dafreurekadetails.dto.servicedto.ServerInstanceDetail;
import com.dafreurekadetails.dto.servicedto.ServiceDetail;
import com.dafreurekadetails.dto.servicedto.ServiceGroup;
import com.dafreurekadetails.service.EurekaQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EurekaQueryController.class)
class EurekaQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EurekaQueryService eurekaQueryService;


    @Test
    void shouldSuccessfullyGetAppsWithServersGroupBy() throws Exception {
        EurekaQueryRequest request = new EurekaQueryRequest(
                "http://localhost:8761/eureka",
                "servers"
        );

        EurekaQueryResponse<GroupedResult> mockResponse = createMockServerResponse();

        when(eurekaQueryService.handleQuery(eq(request.groupBy()), eq(request.eurekaServerURL())))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/cdi-eureka-service/v1/eureka/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(ReturnCode.SUCCESS.status()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.returnCode").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Query executed successfully"))
                .andExpect(jsonPath("$.httpStatusCode").value(200))
                .andExpect(jsonPath("$.transactionID").value("TXN-12345"))
                .andExpect(jsonPath("$.elapsedTime").value(200))
                .andExpect(jsonPath("$.servers").isArray())
                .andExpect(jsonPath("$.servers[0].server.hostName").value("localhost"))
                .andExpect(jsonPath("$.servers[0].server.services[0].service.serviceName").value("MY-SERVICE"));

        verify(eurekaQueryService, times(1))
                .handleQuery("servers", "http://localhost:8761/eureka");
    }

    @Test
    void shouldSuccessfullyGetAppsWithServicesGroupBy() throws Exception {
        EurekaQueryRequest request = new EurekaQueryRequest(
                "http://localhost:8761/eureka",
                "services"
        );

        EurekaQueryResponse<GroupedResult> mockResponse = createMockServiceResponse();

        when(eurekaQueryService.handleQuery(eq(request.groupBy()), eq(request.eurekaServerURL())))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/cdi-eureka-service/v1/eureka/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(ReturnCode.SUCCESS.status()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.returnCode").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Query executed successfully"))
                .andExpect(jsonPath("$.httpStatusCode").value(200))
                .andExpect(jsonPath("$.transactionID").value("TXN-12345"))
                .andExpect(jsonPath("$.elapsedTime").value(200))
                .andExpect(jsonPath("$.services").isArray())
                .andExpect(jsonPath("$.services",hasSize(2)))
                .andExpect(jsonPath("$.services[0].service.serviceName").value("SERVICE-1"))
                .andExpect(jsonPath("$.services[1].service.serviceName").value("SERVICE-2"))
                .andExpect(jsonPath("$.services[0].service.servers",hasSize(2)))
                .andExpect(jsonPath("$.services[0].service.servers[0].server.hostname").value("SERVER-1"))
                .andExpect(jsonPath("$.services[0].service.servers[1].server.hostname").value("SERVER-2"))
                .andExpect(jsonPath("$.services[0].service.servers[0].server.instanceDetail.ipAddr").value("127.0.0.1"));

        verify(eurekaQueryService).handleQuery("services", "http://localhost:8761/eureka");
    }

    @Test
    void shouldHandleCaseInsensitiveGroupByValues() throws Exception {
        EurekaQueryRequest request = new EurekaQueryRequest(
                "http://localhost:8761/eureka",
                "SERVERS"
        );

        EurekaQueryResponse<GroupedResult> mockResponse = createMockServerResponse();
        when(eurekaQueryService.handleQuery(eq("SERVERS"), any(String.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/cdi-eureka-service/v1/eureka/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(ReturnCode.SUCCESS.status()));

        verify(eurekaQueryService).handleQuery("SERVERS", "http://localhost:8761/eureka");
    }


    @Test
    void shouldReturnStructuredValidationErrorResponse() throws Exception {
        String invalidRequest = """
            {
              "eurekaServerURL": "http://localhost:8761/eureka",
              "groupBy": "invalidValue"
            }
            """;

        mockMvc.perform(post("/cdi-eureka-service/v1/eureka/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.returnCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("groupBy")))
                .andExpect(jsonPath("$.message").value(containsString("must be either 'servers' or 'services'")))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(eurekaQueryService, never()).handleQuery(any(), any());
    }

    @Test
    void shouldReturnValidationErrorForBlankGroupBy() throws Exception {
        String requestWithBlankGroupBy = """
        {
          "eurekaServerURL": "http://localhost:8761/eureka",
          "groupBy": ""
        }
        """;

        mockMvc.perform(post("/cdi-eureka-service/v1/eureka/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithBlankGroupBy))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.returnCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("groupBy")))
                .andExpect(jsonPath("$.message").value(containsString("required")))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(eurekaQueryService, never()).handleQuery(any(), any());
    }

    @Test
    void shouldReturnValidationErrorForInvalidEurekaUrlFormat() throws Exception {
        String invalidUrlRequest = """
        {
          "eurekaServerURL": "localhost:8761",
          "groupBy": "servers"
        }
        """;

        mockMvc.perform(post("/cdi-eureka-service/v1/eureka/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUrlRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.returnCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("Eureka server URL")))
                .andExpect(jsonPath("$.message").value(containsString("http:// or https://")))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(eurekaQueryService, never()).handleQuery(any(), any());
    }


    @Test
    void shouldReturnValidationErrorForBlankEurekaUrl() throws Exception {
        // Given
        String blankUrlRequest = """
        {
          "eurekaServerURL": "",
          "groupBy": "servers"
        }
        """;

        mockMvc.perform(post("/cdi-eureka-service/v1/eureka/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blankUrlRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.returnCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Eureka server URL must not be blank")))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(eurekaQueryService, never()).handleQuery(any(), any());
    }


    @Test
    void shouldReturnValidationErrorForNullValues() throws Exception {
        String nullValuesRequest = """
        {
          "eurekaServerURL": null,
          "groupBy": null
        }
        """;

        mockMvc.perform(post("/cdi-eureka-service/v1/eureka/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nullValuesRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.returnCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Eureka server URL must not be blank")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("GroupBy parameter is required.")))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(eurekaQueryService, never()).handleQuery(any(), any());
    }

    @Test
    void shouldReturnBadRequestForMalformedJson() throws Exception {
        String malformedJson = "{ \"eurekaServerURL\": \"http://localhost:8761/eureka\", \"groupBy\": ";

        mockMvc.perform(post("/cdi-eureka-service/v1/eureka/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.returnCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Malformed JSON")))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(eurekaQueryService, never()).handleQuery(any(), any());
    }



    private EurekaQueryResponse<GroupedResult> createMockServerResponse() {
        BaseInstanceDetail instanceDetail = createBaseInstanceDetail();

        ServiceInstanceDetail serviceDetail = new ServiceInstanceDetail("MY-SERVICE", instanceDetail);
        ServiceInstance serviceInstance = new ServiceInstance(serviceDetail);
        ServerDetail serverDetail = new ServerDetail("localhost", List.of(serviceInstance));
        ServerGroup serverGroup = new ServerGroup(serverDetail);
        ServerResult serverResult = new ServerResult(List.of(serverGroup));

        return EurekaQueryResponse.from(
                ReturnCode.SUCCESS,
                "Query executed successfully",
                "TXN-12345",
                200,
                serverResult
        );
    }
    private EurekaQueryResponse<GroupedResult> createMockServiceResponse() {
        BaseInstanceDetail instanceDetail = createBaseInstanceDetail();

        ServerInstanceDetail serverInstanceDetail1 = new ServerInstanceDetail("SERVER-1",instanceDetail);
        ServerInstance serverInstance1 = new ServerInstance(serverInstanceDetail1);
        ServerInstanceDetail serverInstanceDetail2 = new ServerInstanceDetail("SERVER-2",instanceDetail);
        ServerInstance serverInstance2 = new ServerInstance(serverInstanceDetail2);

        ServiceDetail serviceDetail1 = new ServiceDetail("SERVICE-1",List.of(serverInstance1,serverInstance2));
        ServiceGroup serviceGroup1 = new ServiceGroup(serviceDetail1);
        ServiceDetail serviceDetail2 = new ServiceDetail("SERVICE-2",List.of(serverInstance1,serverInstance2));
        ServiceGroup serviceGroup2 = new ServiceGroup(serviceDetail2);

        ServiceResult serviceResult = new ServiceResult(List.of(serviceGroup1,serviceGroup2));

        return EurekaQueryResponse.from(
                ReturnCode.SUCCESS,
                "Query executed successfully",
                "TXN-12345",
                200,
                serviceResult
        );
    }
    private BaseInstanceDetail createBaseInstanceDetail(){
        return new BaseInstanceDetail(
                "127.0.0.1", 8080, 8443, "http://127.0.0.1:8080", "http://127.0.0.1:8080/home",
                "http://127.0.0.1:8080/status", "UP",
                System.currentTimeMillis(), System.currentTimeMillis(),
                false,
                new Metadata("1.0", "EU", "zone-1", "t2.micro", "build-123"),
                new LeaseInfo(30, 90, System.currentTimeMillis(), System.currentTimeMillis(), 0, System.currentTimeMillis())
        );
    }

}
