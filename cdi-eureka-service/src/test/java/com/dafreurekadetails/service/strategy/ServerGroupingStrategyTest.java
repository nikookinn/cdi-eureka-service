package com.dafreurekadetails.service.strategy;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.ServerResult;
import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.dafreurekadetails.dto.serverdto.ServerGroup;
import com.dafreurekadetails.exception.*;
import com.dafreurekadetails.mapper.InstanceMapper;
import com.dafreurekadetails.service.EurekaClientHelper;
import com.fasterxml.jackson.databind.JsonNode;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ServerGroupingStrategyTest {
    @Mock
    private EurekaClientHelper eurekaClientHelper;

    @Mock
    private InstanceMapper instanceMapper;

    @InjectMocks
    private ServerGroupingStrategy serverGroupingStrategy;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Constructor should throw exception when EurekaClientHelper is null")
    void constructor_ShouldThrowException_WhenEurekaClientHelperIsNull() {
        assertThatThrownBy(() -> new ServerGroupingStrategy(null, instanceMapper))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("EurekaClientHelper cannot be null");
    }

    @Test
    @DisplayName("Constructor should throw exception when InstanceMapper is null")
    void constructor_ShouldThrowException_WhenInstanceMapperIsNull() {
        assertThatThrownBy(() -> new ServerGroupingStrategy(eurekaClientHelper, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("InstanceMapper cannot be null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Group should throw InvalidRequestException when Eureka server URL is blank")
    void group_ShouldThrowInvalidRequestException_WhenEurekaServerUrlIsBlank(String url) {
        assertThatThrownBy(() -> serverGroupingStrategy.group(url))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Invalid eureka server url");
    }

    @Test
    @DisplayName("Group should throw ServiceUnavailableException when Eureka client returns null")
    void group_ShouldThrowServiceUnavailableException_WhenEurekaClientReturnsNull() {
        String eurekaUrl = "http://localhost:8761/eureka";
        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(null);

        assertThatThrownBy(() -> serverGroupingStrategy.group(eurekaUrl))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("Eureka server does not response" + eurekaUrl);
    }

    @Test
    @DisplayName("Group should return empty ServerResult when no applications exist")
    void group_ShouldReturnEmptyServerResult_WhenNoApplicationsExist() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithEmptyApplications();
        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).isEmpty();
    }

    @Test
    @DisplayName("Group should return ServerResult with single server hosting single service")
    void group_ShouldReturnServerResult_WithSingleServerHostingSingleService() {
        String eurekaUrl = "http://localhost:8761/eureka";
        String serviceName = "CDI-EUREKA-SERVICE";
        String hostname = "localhost";

        ObjectNode root = createRootNodeWithSingleApplication(serviceName, hostname);
        BaseInstanceDetail mockInstanceDetail = createMockInstanceDetail();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class))).thenReturn(mockInstanceDetail);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).hasSize(1);

        ServerGroup serverGroup = serverResult.servers().get(0);
        assertThat(serverGroup.server().hostName()).isEqualTo(hostname);
        assertThat(serverGroup.server().services()).hasSize(1);
        assertThat(serverGroup.server().services().get(0).service().serviceName()).isEqualTo(serviceName);
        assertThat(serverGroup.server().services().get(0).service().instanceDetail()).isEqualTo(mockInstanceDetail);
    }

    @Test
    @DisplayName("Group should return ServerResult with single server hosting multiple services")
    void group_ShouldReturnServerResult_WithSingleServerHostingMultipleServices() {
        String eurekaUrl = "http://localhost:8761/eureka";
        String hostname = "server-1";
        ObjectNode root = createRootNodeWithMultipleServicesOnSameHost(hostname);
        BaseInstanceDetail mockInstanceDetail = createMockInstanceDetail();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class))).thenReturn(mockInstanceDetail);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).hasSize(1);

        ServerGroup serverGroup = serverResult.servers().get(0);
        assertThat(serverGroup.server().hostName()).isEqualTo(hostname);
        assertThat(serverGroup.server().services()).hasSize(2);

        List<String> serviceNames = serverGroup.server().services().stream()
                .map(si -> si.service().serviceName())
                .toList();
        assertThat(serviceNames).containsExactlyInAnyOrder("SERVICE-A", "SERVICE-B");
    }

    @Test
    @DisplayName("Group should return ServerResult with multiple servers hosting different services")
    void group_ShouldReturnServerResult_WithMultipleServersHostingDifferentServices() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithMultipleServers();
        BaseInstanceDetail mockInstanceDetail = createMockInstanceDetail();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class))).thenReturn(mockInstanceDetail);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).hasSize(2);

        List<String> hostnames = serverResult.servers().stream()
                .map(sg -> sg.server().hostName())
                .toList();
        assertThat(hostnames).containsExactlyInAnyOrder("server-1", "server-2");
    }

    @Test
    @DisplayName("Group should handle service with multiple instances on different hosts")
    void group_ShouldHandleService_WithMultipleInstancesOnDifferentHosts() {
        String eurekaUrl = "http://localhost:8761/eureka";
        String serviceName = "DISTRIBUTED-SERVICE";
        ObjectNode root = createRootNodeWithServiceOnMultipleHosts(serviceName);
        BaseInstanceDetail mockInstanceDetail = createMockInstanceDetail();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class))).thenReturn(mockInstanceDetail);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).hasSize(2);

        // Verify each server has one instance of the same service
        for (ServerGroup serverGroup : serverResult.servers()) {
            assertThat(serverGroup.server().services()).hasSize(1);
            assertThat(serverGroup.server().services().get(0).service().serviceName()).isEqualTo(serviceName);
        }

        List<String> hostnames = serverResult.servers().stream()
                .map(sg -> sg.server().hostName())
                .toList();
        assertThat(hostnames).containsExactlyInAnyOrder("host-1", "host-2");
    }

    @Test
    @DisplayName("Group should skip application with empty service name")
    void group_ShouldSkipApplication_WithEmptyServiceName() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithEmptyServiceName();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).isEmpty();
    }

    @Test
    @DisplayName("Group should skip application with no instances")
    void group_ShouldSkipApplication_WithNoInstances() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithNoInstances();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).isEmpty();
    }

    @Test
    @DisplayName("Group should skip instance with empty hostname")
    void group_ShouldSkipInstance_WithEmptyHostname() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithBlankHostname();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).isEmpty();
    }

    @Test
    @DisplayName("Group should handle applications array as non-array gracefully")
    void group_ShouldHandleApplicationsArray_AsNonArrayGracefully() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithNonArrayApplications();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).isEmpty();
    }

    @Test
    @DisplayName("Group should handle instance array as non-array gracefully")
    void group_ShouldHandleInstanceArray_AsNonArrayGracefully() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithNonArrayInstances();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).isEmpty();
    }

    @Test
    @DisplayName("Group should filter out servers with no valid services")
    void group_ShouldFilterOutServers_WithNoValidServices() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ObjectNode root = createRootNodeWithInvalidInstances();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class))).thenReturn(null);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).isEmpty();
    }

    @Test
    @DisplayName("Group should throw InstanceMappingException when mapper fails with ApiException")
    void group_ShouldThrowInstanceMappingException_WhenMapperFailsWithApiException() {
        String eurekaUrl = "http://localhost:8761/eureka";
        String serviceName = "TEST-SERVICE";
        ObjectNode root = createRootNodeWithSingleApplication(serviceName, "localhost");

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class)))
                .thenThrow(new InstanceMappingException("Mapping failed"));

        assertThatThrownBy(() -> serverGroupingStrategy.group(eurekaUrl))
                .isInstanceOf(InstanceMappingException.class)
                .hasMessage("Failed to map instance for service: " + serviceName);
    }

    @Test
    @DisplayName("Group should throw GroupingException when unexpected exception occurs")
    void group_ShouldThrowGroupingException_WhenUnexpectedExceptionOccurs() {
        String eurekaUrl = "http://localhost:8761/eureka";
        when(eurekaClientHelper.getEurekaApps(eurekaUrl))
                .thenThrow(new RuntimeException("Unexpected error"));

        assertThatThrownBy(() -> serverGroupingStrategy.group(eurekaUrl))
                .isInstanceOf(GroupingException.class)
                .hasMessage("Failed to group by server :" + eurekaUrl);
    }

    @Test
    @DisplayName("Group should re-throw ApiException when it occurs from EurekaClientHelper")
    void group_ShouldReThrowApiException_WhenItOccursFromEurekaClientHelper() {
        String eurekaUrl = "http://localhost:8761/eureka";
        ApiException apiException = new ServiceUnavailableException("Service down");
        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenThrow(apiException);

        assertThatThrownBy(() -> serverGroupingStrategy.group(eurekaUrl))
                .isInstanceOf(ApiException.class)
                .hasMessage("Service down");
    }

    @Test
    @DisplayName("Group should handle real Eureka response structure")
    void group_ShouldHandleRealEurekaResponse_Structure() {
        String eurekaUrl = "http://localhost:8762/eureka";
        ObjectNode root = createRealEurekaResponseStructure();
        BaseInstanceDetail mockInstanceDetail = createMockInstanceDetail();

        when(eurekaClientHelper.getEurekaApps(eurekaUrl)).thenReturn(root);
        when(instanceMapper.mapToBaseInstanceDetail(any(JsonNode.class))).thenReturn(mockInstanceDetail);

        GroupedResult result = serverGroupingStrategy.group(eurekaUrl);

        assertThat(result).isInstanceOf(ServerResult.class);
        ServerResult serverResult = (ServerResult) result;
        assertThat(serverResult.servers()).hasSize(1);

        ServerGroup serverGroup = serverResult.servers().get(0);
        assertThat(serverGroup.server().hostName()).isEqualTo("localhost");
        assertThat(serverGroup.server().services()).hasSize(1);
        assertThat(serverGroup.server().services().get(0).service().serviceName()).isEqualTo("CDI-EUREKA-SERVICE");
    }

}

