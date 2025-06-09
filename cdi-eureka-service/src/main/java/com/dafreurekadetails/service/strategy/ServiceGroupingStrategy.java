package com.dafreurekadetails.service.strategy;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.ServiceResult;
import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.dafreurekadetails.dto.servicedto.ServiceGroup;
import com.dafreurekadetails.dto.servicedto.ServiceDetail;
import com.dafreurekadetails.dto.servicedto.ServerInstance;
import com.dafreurekadetails.dto.servicedto.ServerInstanceDetail;
import com.dafreurekadetails.exception.*;
import com.dafreurekadetails.logger.AppLogger;
import com.dafreurekadetails.mapper.InstanceMapper;
import com.dafreurekadetails.service.EurekaClientHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

@Component("services")
public class ServiceGroupingStrategy implements GroupingStrategy {

    private static final AppLogger APP_LOGGER = AppLogger.getLogger(ServiceGroupingStrategy.class);
    private final EurekaClientHelper eurekaClient;
    private final InstanceMapper mapper;

    public ServiceGroupingStrategy(EurekaClientHelper eurekaClient, InstanceMapper mapper) {
        this.eurekaClient = Objects.requireNonNull(eurekaClient, "EurekaClientHelper cannot be null");
        this.mapper = Objects.requireNonNull(mapper, "InstanceMapper cannot be null");
    }

    @Override
    public GroupedResult group(String eurekaServerUrl) {
        Objects.requireNonNull(eurekaServerUrl, "Eureka server URL cannot be null");

        if (!StringUtils.hasText(eurekaServerUrl)) {
            throw new InvalidRequestException("Eureka server URL cannot be null or blank");
        }

        try {
            APP_LOGGER.debug("Grouping services for Eureka server {}", eurekaServerUrl);

            JsonNode root = eurekaClient.getEurekaApps(eurekaServerUrl);
            if (root == null || root.isMissingNode()) {
                APP_LOGGER.warn("No data from Eureka server {}", eurekaServerUrl);
                throw  new ServiceUnavailableException("Eureka server does not response"+eurekaServerUrl);
            }

            ArrayNode apps = extractApplicationsArray(root);
            if (apps == null || apps.isEmpty()) {
                APP_LOGGER.info("Eureka server {} has no applications", eurekaServerUrl);
                return new ServiceResult(List.of());
            }

            List<ServiceGroup> groups = processApplications(apps);

            return new ServiceResult(groups);

        }catch (ApiException ex){
            APP_LOGGER.error(ex.getMessage(), eurekaServerUrl);
            throw ex;
        }
        catch (Exception ex) {
            throw new GroupingException("Failed to group by service :"+eurekaServerUrl,ex);
        }
    }

    private ArrayNode extractApplicationsArray(JsonNode root) {
        JsonNode appsNode = root.path("applications").path("application");
        return appsNode.isArray() ? (ArrayNode) appsNode : null;
    }

    private List<ServiceGroup> processApplications(ArrayNode apps) {
        List<ServiceGroup> groups = new ArrayList<>();
        for (JsonNode app : apps) {
                ServiceGroup sg = processApplication(app);
                if (sg != null) groups.add(sg);
        }
        return groups;
    }

    private ServiceGroup processApplication(JsonNode app) {
        String serviceName = app.path("name").asText();
        if (!StringUtils.hasText(serviceName)) {
            APP_LOGGER.warn("Application name empty – skipping");
            return null;
        }

        JsonNode instanceNode = app.path("instance");
        if (instanceNode.isMissingNode()) {
            APP_LOGGER.debug("Service {} has no instances", serviceName);
            return null;
        }

        ArrayNode instances = instanceNode.isArray()
                ? (ArrayNode) instanceNode
                : singletonArray(instanceNode);

        List<ServerInstance> servers = buildServerInstances(instances);

        return servers.isEmpty()
                ? null
                : new ServiceGroup(
                new ServiceDetail(serviceName, servers)
        );
    }

    private ArrayNode singletonArray(JsonNode node) {
        ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        arr.add(node);
        return arr;
    }

    private List<ServerInstance> buildServerInstances(ArrayNode instances) {
        List<ServerInstance> result = new ArrayList<>();

        for (JsonNode instance : instances) {
            try {
                String hostname = determineHostName(instance);
                BaseInstanceDetail det = mapper.mapToBaseInstanceDetail(instance);

                if (det != null) {
                    ServerInstanceDetail sid = new ServerInstanceDetail(hostname, det);
                    result.add(new ServerInstance(sid));
                }

            } catch (Exception ex) {
                APP_LOGGER.warn("Error mapping instance for service {}: {}", instance.path("instanceId").asText(), ex.getMessage());
                throw new InstanceMappingException(instance.path("instanceId").asText("unknown"),ex);
            }
        }
        return result;
    }

    private String determineHostName(JsonNode instanceNode) {
        String host = instanceNode.path("hostName").asText();
        if (!host.isBlank()) return host;

        host = instanceNode.path("ipAddr").asText();
        if (!host.isBlank()) return host;

        String fallback = instanceNode.path("instanceId").asText("unknown-host");
        APP_LOGGER.warn("No hostName/ipAddr – using instanceId {}", fallback);
        return fallback;
    }
}
