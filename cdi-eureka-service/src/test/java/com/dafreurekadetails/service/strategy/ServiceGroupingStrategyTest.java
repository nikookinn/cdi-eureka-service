package com.dafreurekadetails.service.strategy;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.ServiceResult;
import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.dafreurekadetails.dto.servicedto.ServiceGroup;
import com.dafreurekadetails.exception.*;
import com.dafreurekadetails.mapper.InstanceMapper;
import com.dafreurekadetails.service.EurekaClientHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.dafreurekadetails.service.strategy.EurekaTestDataBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class ServiceGroupingStrategyTest {
    @Mock
    private EurekaClientHelper eurekaClientHelper;

    @Mock
    private InstanceMapper instanceMapper;

    @InjectMocks
    private ServiceGroupingStrategy serviceGroupingStrategy;

    private JsonNodeFactory nodeFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        nodeFactory = JsonNodeFactory.instance;
    }

    @Test
    @DisplayName("Constructor should throw exception when EurekaClientHelper is null")
    void constructor_ShouldThrowException_WhenEurekaClientHelperIsNull() {
        assertThatThrownBy(() -> new ServiceGroupingStrategy(null, instanceMapper))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("EurekaClientHelper cannot be null");
    }

    @Test
    @DisplayName("Constructor should throw exception when InstanceMapper is null")
    void constructor_ShouldThrowException_WhenInstanceMapperIsNull() {
        assertThatThrownBy(() -> new ServiceGroupingStrategy(eurekaClientHelper, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("InstanceMapper cannot be null");
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Group should throw InvalidRequestException when Eureka server URL is blank")
    void group_ShouldThrowInvalidRequestException_WhenEurekaServerUrlIsBlank(String url) {
        assertThatThrownBy(() -> serviceGroupingStrategy.group(url))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Eureka server URL cannot be null or blank");
    }

    @Test
    @DisplayName("Group should throw ServiceUnavailableException when Eureka client returns null")
    void group_ShouldThrowServiceUnavailableException_WhenEurekaClientReturnsNull() {
        String eurekaUrl = "http://localhost:8761/eureka";
        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(null);

        assertThatThrownBy(() -> serviceGroupingStrategy.group(eurekaUrl))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("Eureka server does not response" + eurekaUrl);
    }

    @Test
    @DisplayName("Group should throw ServiceUnavailableException when Eureka client returns missing node")
    void group_ShouldThrowServiceUnavailableException_WhenEurekaClientReturnsMissingNode() {
        String eurekaUrl = "http://localhost:8761/eureka";
        JsonNode missingNode = nodeFactory.missingNode();
        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(missingNode);

        assertThatThrownBy(() -> serviceGroupingStrategy.group(eurekaUrl))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("Eureka server does not response" + eurekaUrl);
    }

    @Test
    @DisplayName("Group should return empty ServiceResult when no applications exist")
    void group_ShouldReturnEmptyServiceResult_WhenNoApplicationsExist() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithEmptyApplications();
        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);

        GroupedResult result = serviceGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServiceResult.class);
        ServiceResult serviceResult = (ServiceResult) result;
        assertThat(serviceResult.services()).isEmpty();
    }

    @Test
    @DisplayName("Group should return ServiceResult with single service and single instance")
    void group_ShouldReturnServiceResult_WithSingleServiceAndSingleInstance() {
        String eurekaUrl = "http://localhost:8761/eureka";
        String serviceName = "TEST-SERVICE";
        String hostname = "localhost";

        ObjectNode root = createRootNodeWithSingleApplication(serviceName, hostname);
        BaseInstanceDetail mockInstanceDetail = createMockInstanceDetail();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class))).thenReturn(mockInstanceDetail);

        GroupedResult result = serviceGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServiceResult.class);
        ServiceResult serviceResult = (ServiceResult) result;
        assertThat(serviceResult.services()).hasSize(1);

        ServiceGroup serviceGroup = serviceResult.services().get(0);
        assertThat(serviceGroup.service().serviceName()).isEqualTo(serviceName);
        assertThat(serviceGroup.service().servers()).hasSize(1);
        assertThat(serviceGroup.service().servers().get(0).server().hostname()).isEqualTo(hostname);
        assertThat(serviceGroup.service().servers().get(0).server().instanceDetail()).isEqualTo(mockInstanceDetail);
    }

    @Test
    @DisplayName("Group should return ServiceResult with multiple services")
    void group_ShouldReturnServiceResult_WithMultipleServices() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithMultipleApplications();
        BaseInstanceDetail mockInstanceDetail = createMockInstanceDetail();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class))).thenReturn(mockInstanceDetail);

        GroupedResult result = serviceGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServiceResult.class);
        ServiceResult serviceResult = (ServiceResult) result;
        assertThat(serviceResult.services()).hasSize(2);

        List<String> serviceNames = serviceResult.services().stream()
                .map(sg -> sg.service().serviceName())
                .toList();
        assertThat(serviceNames).containsExactlyInAnyOrder("SERVICE-A", "SERVICE-B");
    }

    @Test
    @DisplayName("Group should handle application with multiple instances")
    void group_ShouldHandleApplication_WithMultipleInstances() {
        String eurekaUrl = "http://localhost:8761/eureka";
        String serviceName = "MULTI-INSTANCE-SERVICE";
        ObjectNode root = createRootNodeWithMultipleInstances(serviceName);
        BaseInstanceDetail mockInstanceDetail = createMockInstanceDetail();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class))).thenReturn(mockInstanceDetail);

        GroupedResult result = serviceGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServiceResult.class);
        ServiceResult serviceResult = (ServiceResult) result;
        assertThat(serviceResult.services()).hasSize(1);

        ServiceGroup serviceGroup = serviceResult.services().get(0);
        assertThat(serviceGroup.service().serviceName()).isEqualTo(serviceName);
        assertThat(serviceGroup.service().servers()).hasSize(2);

        List<String> hostnames = serviceGroup.service().servers().stream()
                .map(si -> si.server().hostname())
                .toList();
        assertThat(hostnames).containsExactlyInAnyOrder("host1", "host2");
    }

    @Test
    @DisplayName("Group should skip application with empty service name")
    void group_ShouldSkipApplication_WithEmptyServiceName() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithEmptyServiceName();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);

        GroupedResult result = serviceGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServiceResult.class);
        ServiceResult serviceResult = (ServiceResult) result;
        assertThat(serviceResult.services()).isEmpty();
    }

    @Test
    @DisplayName("Group should skip application with no instances")
    void group_ShouldSkipApplication_WithNoInstances() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithNoInstances();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);

        GroupedResult result = serviceGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServiceResult.class);
        ServiceResult serviceResult = (ServiceResult) result;
        assertThat(serviceResult.services()).isEmpty();
    }

    @Test
    @DisplayName("Group should use ipAddr when hostname is blank")
    void group_ShouldUseIpAddr_WhenHostnameIsBlank() {
        String eurekaUrl = "http://localhost:8761/eureka";
        String serviceName = "TEST-SERVICE";
        String ipAddr = "192.168.1.100";

        ObjectNode root = createRootNodeWithBlankHostname(serviceName, ipAddr);
        BaseInstanceDetail mockInstanceDetail = createMockInstanceDetail();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class))).thenReturn(mockInstanceDetail);

        GroupedResult result = serviceGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServiceResult.class);
        ServiceResult serviceResult = (ServiceResult) result;
        assertThat(serviceResult.services()).hasSize(1);
        assertThat(serviceResult.services().get(0).service().servers().get(0).server().hostname()).isEqualTo(ipAddr);
    }



    @Test
    @DisplayName("Group should throw InstanceMappingException when mapper fails")
    void group_ShouldThrowInstanceMappingException_WhenMapperFails() {
        String eurekaUrl = "http://localhost:8761/eureka";
        String serviceName = "TEST-SERVICE";
        ObjectNode root = createRootNodeWithSingleApplication(serviceName, "localhost");

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class)))
                .thenThrow(new InstanceMappingException("Mapping failed"));

        assertThatThrownBy(() -> serviceGroupingStrategy.group(eurekaUrl))
                .isInstanceOf(InstanceMappingException.class);
    }

    @Test
    @DisplayName("Group should throw GroupingException when unexpected exception occurs")
    void group_ShouldThrowGroupingException_WhenUnexpectedExceptionOccurs() {
        String eurekaUrl = "http://localhost:8761/eureka";
        when(eurekaClientHelper.getEurekaApps(eurekaUrl))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThatThrownBy(() -> serviceGroupingStrategy.group(eurekaUrl))
                .isInstanceOf(GroupingException.class)
                .hasMessage("Failed to group by service :" + eurekaUrl);
    }

    @Test
    @DisplayName("Group should re-throw ApiException when it occurs")
    void group_ShouldReThrowApiException_WhenItOccurs() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ApiException apiException = new ServiceUnavailableException("Service down");
        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenThrow(apiException);

        assertThatThrownBy(() -> serviceGroupingStrategy.group(eurekaUrl))
                .isInstanceOf(ApiException.class)
                .hasMessage("Service down");
    }
}
