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
/**
 * ServerGroupingStrategy is a grouping strategy implementation that organizes service instances
 * by the servers (host machines) on which they are running.
 * <p>
 * It fetches all registered service instances from the Eureka server, determines their hosting servers
 * (using hostName or a fallback identifier), and groups the services under their respective servers.
 * <p>
 * This strategy is triggered when the grouping type is set to "servers".
 */
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

    /**
     * Groups Eureka service instances by server (hostname) for the specified Eureka server URL.
     *
     * @param eurekaServerUrl the URL of the Eureka server to query
     * @return a {@link GroupedResult} containing service instances grouped by server(host)
     * @throws InvalidRequestException if the provided URL is empty or null
     * @throws ServiceUnavailableException if the Eureka server does not respond
     * @throws GroupingException if any unexpected error occurs while grouping the instances
     */
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
    /**
     * Extracts and groups service instances from the Eureka JSON response by their hostnames.
     *
     * @param root the root JSON node returned by the Eureka server
     * @return a map where keys are server names(hostnames) and values are lists of {@link ServiceInstance}s running on those hosts
     */

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

    /**
     * Processes a single application node from the Eureka JSON response and populates the host-based grouping map.
     *
     * @param appNode the JSON node representing a registered application in Eureka
     * @param byHost the map to store service instances grouped by host
     */
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

    /**
     * Attempts to map a Eureka instance to an internal {@link ServiceInstance} object
     * and adds it to the host-based grouping map.
     *
     * @param serviceName the name of the service/application
     * @param instNode the JSON node representing the instance
     * @param byHost the map to store service instances grouped by host
     * @throws InstanceMappingException if instance mapping fails due to API or data issues
     */
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

        } catch (ApiException ex) {
            APP_LOGGER.warn("Error mapping instance for server {}: {}", serviceName, ex.getMessage());
            throw new InstanceMappingException("Failed to map instance for service: " + serviceName,ex);
        }
    }

    /**
     * Converts the hostname-based instance map into a list of {@link ServerGroup} objects.
     *
     * @param byHost the map of service instances grouped by host
     * @return a list of {@link ServerGroup} each containing details for a specific server and its running services
     */
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
