package com.dafreurekadetails.service.strategy;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.ServerResult;
import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.dafreurekadetails.dto.serverdto.ServerDetail;
import com.dafreurekadetails.dto.serverdto.ServerGroup;
import com.dafreurekadetails.dto.serverdto.ServiceInstance;
import com.dafreurekadetails.dto.serverdto.ServiceInstanceDetail;
import com.dafreurekadetails.exception.*;
import com.dafreurekadetails.logger.AppLogger;
import com.dafreurekadetails.mapper.InstanceMapper;
import com.dafreurekadetails.service.EurekaClientHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component("servers")
public class ServerGroupingStrategy implements GroupingStrategy {

    private static final AppLogger APP_LOGGER = AppLogger.getLogger(ServerGroupingStrategy.class);
    private final EurekaClientHelper eurekaClient;
    private final InstanceMapper instanceMapper;

    public ServerGroupingStrategy(EurekaClientHelper eurekaClient,
                                  InstanceMapper instanceMapper) {
        this.eurekaClient   = Objects.requireNonNull(eurekaClient,   "EurekaClientHelper cannot be null");
        this.instanceMapper = Objects.requireNonNull(instanceMapper, "InstanceMapper cannot be null");
    }

    @Override
    public GroupedResult group(String eurekaServerUrl) {
        if (!StringUtils.hasText(eurekaServerUrl)) {
            throw new InvalidRequestException("Invalid eureka server url");
        }

        APP_LOGGER.debug("Starting server grouping for Eureka URL: {}", eurekaServerUrl);

        try {
            JsonNode root = eurekaClient.getEurekaApps(eurekaServerUrl);
            if (root == null) {
            throw new ServiceUnavailableException("Eureka server does not response"+eurekaServerUrl);
            }

            Map<String, List<ServiceInstance>> byHost = extractInstancesByHost(root);
            List<ServerGroup> groups = buildServerGroups(byHost);

            APP_LOGGER.info("Grouped {} hosts into {} server groups", byHost.size(), groups.size());
            return new ServerResult(groups);

        }catch (ApiException ex){
            APP_LOGGER.error(ex.getMessage(), eurekaServerUrl);
            throw ex;
        }
        catch (Exception ex) {
            APP_LOGGER.error("Error grouping servers from Eureka URL: {}", eurekaServerUrl, ex);
            throw new GroupingException("Failed to group by server :"+eurekaServerUrl,ex);
        }
    }

    private Map<String, List<ServiceInstance>> extractInstancesByHost(JsonNode root) {
        Map<String, List<ServiceInstance>> byHost = new HashMap<>();

            JsonNode appsNode = root.path("applications").path("application");
            if (!appsNode.isArray()) {
                APP_LOGGER.warn("Eureka response has no applications array");
                return byHost;
            }

            for (JsonNode appNode : appsNode) {
                processApplicationNode(appNode, byHost);
            }
            return byHost;
    }

    private void processApplicationNode(JsonNode appNode, Map<String, List<ServiceInstance>> byHost) {
        String serviceName = appNode.path("name").asText(null);
        if (!StringUtils.hasText(serviceName)) {
            APP_LOGGER.debug("Skipping application with empty name");
            return;
        }
        JsonNode instArray = appNode.path("instance");
        if (!instArray.isArray()) {
            APP_LOGGER.debug("Service {} has no instances", serviceName);
            return;
        }
        for (JsonNode inst : instArray) {
                addInstance(serviceName, inst, byHost);
        }
    }

    private void addInstance(String serviceName,
                             JsonNode instNode,
                             Map<String, List<ServiceInstance>> byHost) {

        String hostname = instNode.path("hostName").asText(null);
        if (!StringUtils.hasText(hostname)) {
            APP_LOGGER.debug("Instance for service {} has empty hostname", serviceName);
            return;
        }

        try {
            BaseInstanceDetail detail = instanceMapper.mapToBaseInstanceDetail(instNode);
            if (detail == null) {
                APP_LOGGER.debug("Failed to map BaseInstanceDetail for service {}", serviceName);
                return;
            }

            ServiceInstanceDetail sid = new ServiceInstanceDetail(serviceName, detail);
            ServiceInstance si = new ServiceInstance(sid);

            byHost.computeIfAbsent(hostname, h -> new ArrayList<>()).add(si);
            APP_LOGGER.trace("Added service {} to host {}", serviceName, hostname);

        } catch (Exception ex) {
            APP_LOGGER.warn("Error mapping instance for server {}: {}", serviceName, ex.getMessage());
            throw new InstanceMappingException("Failed to map instance for service: " + serviceName,ex);
        }
    }

    private List<ServerGroup> buildServerGroups(Map<String, List<ServiceInstance>> byHost) {
        return byHost.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> {
                        String host = entry.getKey();
                        List<ServiceInstance> services = entry.getValue()
                                .stream()
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        if (services.isEmpty()) {
                            APP_LOGGER.debug("Host {} filtered out because it has no valid services", host);
                            return null;
                        }
                        ServerDetail detail = new ServerDetail(host, services);
                        return new ServerGroup(detail);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
