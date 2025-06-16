package com.dafreurekadetails.mapper;

import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.dafreurekadetails.dto.base.LeaseInfo;
import com.dafreurekadetails.dto.base.Metadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class InstanceMapperTest {
    @InjectMocks
    private InstanceMapper instanceMapper;
    private JsonNodeFactory nodeFactory;

    @BeforeEach
    void setUp() {
        nodeFactory = JsonNodeFactory.instance;
    }

    @Test
    void mapToBaseInstanceDetail_ShouldMapCompleteInstanceNodeToBaseInstanceDetail() {
        ObjectNode instanceNode = createCompleteInstanceNode();

        BaseInstanceDetail result = instanceMapper.mapToBaseInstanceDetail(instanceNode);

        assertThat(result).isNotNull();
        assertThat(result.ipAddr()).isEqualTo("192.168.1.100");
        assertThat(result.port()).isEqualTo(8080);
        assertThat(result.securePort()).isEqualTo(8443);
        assertThat(result.url()).isEqualTo("http://192.168.1.100:8080/health");
        assertThat(result.homePageUrl()).isEqualTo("http://192.168.1.100:8080/");
        assertThat(result.statusPageUrl()).isEqualTo("http://192.168.1.100:8080/status");
        assertThat(result.status()).isEqualTo("UP");
        assertThat(result.lastUpdatedTimestamp()).isEqualTo(1640995200000L);
        assertThat(result.lastDirtyTimestamp()).isEqualTo(1640995100000L);
        assertThat(result.isCoordinatingDiscoveryServer()).isTrue();
        assertThat(result.metadataMap()).isNotNull();
        assertThat(result.leaseInfo()).isNotNull();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldHandleMissingOptionalFields() {
        ObjectNode instanceNode = nodeFactory.objectNode();
        instanceNode.put("ipAddr", "192.168.1.100");
        instanceNode.put("port", 8080);
        instanceNode.put("status", "UP");

        BaseInstanceDetail result = instanceMapper.mapToBaseInstanceDetail(instanceNode);

        assertThat(result).isNotNull();
        assertThat(result.ipAddr()).isEqualTo("192.168.1.100");
        assertThat(result.port()).isEqualTo(8080);
        assertThat(result.securePort()).isEqualTo(0);
        assertThat(result.url()).isNull();
        assertThat(result.homePageUrl()).isNull();
        assertThat(result.statusPageUrl()).isNull();
        assertThat(result.status()).isEqualTo("UP");
        assertThat(result.lastUpdatedTimestamp()).isEqualTo(0L);
        assertThat(result.lastDirtyTimestamp()).isEqualTo(0L);
        assertThat(result.isCoordinatingDiscoveryServer()).isFalse();
        assertThat(result.metadataMap()).isNull();
        assertThat(result.leaseInfo()).isNull();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldHandleEmptyInstanceNode() {
        ObjectNode instanceNode = nodeFactory.objectNode();

        BaseInstanceDetail result = instanceMapper.mapToBaseInstanceDetail(instanceNode);

        assertThat(result).isNotNull();
        assertThat(result.ipAddr()).isEmpty();
        assertThat(result.port()).isEqualTo(0);
        assertThat(result.securePort()).isEqualTo(0);
        assertThat(result.url()).isNull();
        assertThat(result.homePageUrl()).isNull();
        assertThat(result.statusPageUrl()).isNull();
        assertThat(result.status()).isEmpty();
        assertThat(result.lastUpdatedTimestamp()).isEqualTo(0L);
        assertThat(result.lastDirtyTimestamp()).isEqualTo(0L);
        assertThat(result.isCoordinatingDiscoveryServer()).isFalse();
        assertThat(result.metadataMap()).isNull();
        assertThat(result.leaseInfo()).isNull();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldParseIntegerPortCorrectly() {
        ObjectNode instanceNode = nodeFactory.objectNode();
        instanceNode.put("port", 8080);
        instanceNode.put("securePort", 8443);

        BaseInstanceDetail result = instanceMapper.mapToBaseInstanceDetail(instanceNode);

        assertThat(result.port()).isEqualTo(8080);
        assertThat(result.securePort()).isEqualTo(8443);
    }

    @Test
    void mapToBaseInstanceDetail_ShouldParseStringPortCorrectly() {
        ObjectNode instanceNode = nodeFactory.objectNode();
        instanceNode.put("port", "8080");
        instanceNode.put("securePort", "8443");

        BaseInstanceDetail result = instanceMapper.mapToBaseInstanceDetail(instanceNode);

        assertThat(result.port()).isEqualTo(8080);
        assertThat(result.securePort()).isEqualTo(8443);
    }

    @Test
    void mapToBaseInstanceDetail_ShouldParseComplexPortStructure() {
        ObjectNode instanceNode = nodeFactory.objectNode();
        ObjectNode portNode = nodeFactory.objectNode();
        portNode.put("$", 8080);
        instanceNode.set("port", portNode);

        ObjectNode securePortNode = nodeFactory.objectNode();
        securePortNode.put("$", 8443);
        instanceNode.set("securePort", securePortNode);

        BaseInstanceDetail result = instanceMapper.mapToBaseInstanceDetail(instanceNode);

        assertThat(result.port()).isEqualTo(8080);
        assertThat(result.securePort()).isEqualTo(8443);
    }

    @Test
    void mapToBaseInstanceDetail_ShouldHandleInvalidPortValues() {
        ObjectNode instanceNode = nodeFactory.objectNode();
        instanceNode.put("port", "invalid");
        instanceNode.set("securePort", nodeFactory.arrayNode());

        BaseInstanceDetail result = instanceMapper.mapToBaseInstanceDetail(instanceNode);

        assertThat(result.port()).isEqualTo(0);
        assertThat(result.securePort()).isEqualTo(0);
    }

    @Test
    void mapToBaseInstanceDetail_ShouldMapMetadata_WhenPresent() {
        ObjectNode metadataNode = nodeFactory.objectNode();
        metadataNode.put("management.port", "8081");
        metadataNode.put("zone", "us-east-1a");

        Metadata result = instanceMapper.mapMetadata(metadataNode);

        assertThat(result).isNotNull();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldReturnNullForNullMetadataNode() {
        Metadata result = instanceMapper.mapMetadata(null);

        assertThat(result).isNull();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldReturnNullForMissingMetadataNode() {
        JsonNode missingNode = nodeFactory.objectNode().path("nonexistent");

        Metadata result = instanceMapper.mapMetadata(missingNode);

        assertThat(result).isNull();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldMapLeaseInfo_WhenPresent() {
        ObjectNode leaseInfoNode = nodeFactory.objectNode();
        leaseInfoNode.put("renewalIntervalInSecs", 30);
        leaseInfoNode.put("durationInSecs", 90);

        LeaseInfo result = instanceMapper.mapLeaseInfo(leaseInfoNode);

        assertThat(result).isNotNull();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldReturnNullForNullLeaseInfoNode() {
        LeaseInfo result = instanceMapper.mapLeaseInfo(null);

        assertThat(result).isNull();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldReturnNullForMissingLeaseInfoNode() {
        JsonNode missingNode = nodeFactory.objectNode().path("nonexistent");

        LeaseInfo result = instanceMapper.mapLeaseInfo(missingNode);

        assertThat(result).isNull();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldHandleBooleanValuesCorrectly() {
        ObjectNode instanceNode = nodeFactory.objectNode();
        instanceNode.put("isCoordinatingDiscoveryServer", true);

        BaseInstanceDetail result = instanceMapper.mapToBaseInstanceDetail(instanceNode);

        assertThat(result.isCoordinatingDiscoveryServer()).isTrue();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldHandleStringBooleanValues() {
        ObjectNode instanceNode = nodeFactory.objectNode();
        instanceNode.put("isCoordinatingDiscoveryServer", "true");

        BaseInstanceDetail result = instanceMapper.mapToBaseInstanceDetail(instanceNode);

        assertThat(result.isCoordinatingDiscoveryServer()).isTrue();
    }

    @Test
    void mapToBaseInstanceDetail_ShouldHandleTimestampValuesCorrectly() {
        ObjectNode instanceNode = nodeFactory.objectNode();
        instanceNode.put("lastUpdatedTimestamp", 1640995200000L);
        instanceNode.put("lastDirtyTimestamp", 1640995100000L);

        BaseInstanceDetail result = instanceMapper.mapToBaseInstanceDetail(instanceNode);

        assertThat(result.lastUpdatedTimestamp()).isEqualTo(1640995200000L);
        assertThat(result.lastDirtyTimestamp()).isEqualTo(1640995100000L);
    }

    private ObjectNode createCompleteInstanceNode() {
        ObjectNode instanceNode = nodeFactory.objectNode();
        instanceNode.put("ipAddr", "192.168.1.100");
        instanceNode.put("port", 8080);
        instanceNode.put("securePort", 8443);
        instanceNode.put("healthCheckUrl", "http://192.168.1.100:8080/health");
        instanceNode.put("homePageUrl", "http://192.168.1.100:8080/");
        instanceNode.put("statusPageUrl", "http://192.168.1.100:8080/status");
        instanceNode.put("status", "UP");
        instanceNode.put("lastUpdatedTimestamp", 1640995200000L);
        instanceNode.put("lastDirtyTimestamp", 1640995100000L);
        instanceNode.put("isCoordinatingDiscoveryServer", true);

        ObjectNode metadataNode = nodeFactory.objectNode();
        metadataNode.put("management.port", "8081");
        metadataNode.put("zone", "us-east-1a");
        instanceNode.set("metadata", metadataNode);

        ObjectNode leaseInfoNode = nodeFactory.objectNode();
        leaseInfoNode.put("renewalIntervalInSecs", 30);
        leaseInfoNode.put("durationInSecs", 90);
        instanceNode.set("leaseInfo", leaseInfoNode);

        return instanceNode;
    }
}
