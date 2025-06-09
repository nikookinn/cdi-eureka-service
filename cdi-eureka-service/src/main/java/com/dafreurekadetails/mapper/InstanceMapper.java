package com.dafreurekadetails.mapper;

import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.dafreurekadetails.dto.base.LeaseInfo;
import com.dafreurekadetails.dto.base.Metadata;
import com.dafreurekadetails.dto.servicedto.ServiceDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class InstanceMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BaseInstanceDetail mapToBaseInstanceDetail(JsonNode instanceNode) {

        int port = parsePort(instanceNode.path("port"));
        int securePort = parsePort(instanceNode.path("securePort"));

        return new BaseInstanceDetail(
                instanceNode.path("ipAddr").asText(""),
                port,
                securePort,
                instanceNode.path("healthCheckUrl").asText(null),
                instanceNode.path("homePageUrl").asText(null),
                instanceNode.path("statusPageUrl").asText(null),
                instanceNode.path("status").asText(""),
                instanceNode.path("lastUpdatedTimestamp").asLong(0L),
                instanceNode.path("lastDirtyTimestamp").asLong(0L),
                instanceNode.path("isCoordinatingDiscoveryServer").asBoolean(false),
                mapMetadata(instanceNode.path("metadata")),
                mapLeaseInfo(instanceNode.path("leaseInfo"))
        );
    }

    private int parsePort(JsonNode portNode) {
        if (portNode.isInt() || portNode.isTextual()) {
            return portNode.asInt(0);
        }
        return portNode.path("$").asInt(0);
    }

    public Metadata mapMetadata(JsonNode node) {
        return node == null || node.isMissingNode()
                ? null
                : objectMapper.convertValue(node, Metadata.class);
    }

    public LeaseInfo mapLeaseInfo(JsonNode node) {
        return node == null || node.isMissingNode()
                ? null
                : objectMapper.convertValue(node, LeaseInfo.class);
    }
}
