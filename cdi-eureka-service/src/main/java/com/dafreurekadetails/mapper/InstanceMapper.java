package com.dafreurekadetails.mapper;

import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.dafreurekadetails.dto.base.LeaseInfo;
import com.dafreurekadetails.dto.base.Metadata;
import com.dafreurekadetails.dto.servicedto.ServiceDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
/**
 * Utility component responsible for mapping raw JSON from Eureka
 * into strongly typed Java DTOs like {@link BaseInstanceDetail}, {@link Metadata}, and {@link LeaseInfo}.
 */
@Component
public class InstanceMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Maps a JSON node representing an instance to a {@link BaseInstanceDetail}.
     *
     * @param instanceNode the JSON node from Eureka response
     * @return a mapped {@link BaseInstanceDetail} object
     */
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


    //Handles variations in port representation (either as int, string, or wrapped).
    private int parsePort(JsonNode portNode) {
        if (portNode.isInt() || portNode.isTextual()) {
            return portNode.asInt(0);
        }
        return portNode.path("$").asInt(0);
    }

    //Maps a JSON node to a Metadata object.
    public Metadata mapMetadata(JsonNode node) {
        return node == null || node.isMissingNode()
                ? null
                : objectMapper.convertValue(node, Metadata.class);
    }
    //Maps a JSON node to a LeaseInfo object.
    public LeaseInfo mapLeaseInfo(JsonNode node) {
        return node == null || node.isMissingNode()
                ? null
                : objectMapper.convertValue(node, LeaseInfo.class);
    }
}
