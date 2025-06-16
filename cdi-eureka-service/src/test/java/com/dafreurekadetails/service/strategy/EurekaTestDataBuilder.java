package com.dafreurekadetails.service.strategy;

import com.dafreurekadetails.dto.base.BaseInstanceDetail;
import com.dafreurekadetails.dto.base.LeaseInfo;
import com.dafreurekadetails.dto.base.Metadata;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EurekaTestDataBuilder {
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

    // Constants for commonly used values
    private static final String DEFAULT_IP = "127.0.0.1";
    private static final String DEFAULT_STATUS = "UP";
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_HTTPS_PORT = 443;

    /**
     * Creates an empty Eureka applications structure
     */
    public static ObjectNode createRootNodeWithEmptyApplications() {
        return createRootNode(NODE_FACTORY.arrayNode());
    }

    /**
     * Creates Eureka structure with single application
     */
    public static ObjectNode createRootNodeWithSingleApplication(String serviceName, String hostname) {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();
        ObjectNode app = createApplication(serviceName);
        ArrayNode instanceArray = NODE_FACTORY.arrayNode();
        instanceArray.add(createBasicInstanceNode(hostname, DEFAULT_IP, generateInstanceId(serviceName)));
        app.set("instance", instanceArray);
        applicationArray.add(app);
        return createRootNode(applicationArray);
    }

    /**
     * Creates Eureka structure with multiple services on same host
     */
    public static ObjectNode createRootNodeWithMultipleServicesOnSameHost(String hostname) {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();

        // Service A
        applicationArray.add(createSingleInstanceApplication("SERVICE-A", hostname, DEFAULT_IP));
        // Service B
        applicationArray.add(createSingleInstanceApplication("SERVICE-B", hostname, DEFAULT_IP));

        return createRootNode(applicationArray);
    }

    public static ObjectNode createRootNodeWithMultipleApplications() {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();

        applicationArray.add(createSingleInstanceApplication("SERVICE-A", "host1", DEFAULT_IP));
        applicationArray.add(createSingleInstanceApplication("SERVICE-B", "host2", "127.0.0.2"));

        return createRootNode(applicationArray);
    }

    /**
     * Creates Eureka structure with multiple servers
     */
    public static ObjectNode createRootNodeWithMultipleServers() {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();

        applicationArray.add(createSingleInstanceApplication("SERVICE-A", "server-1", DEFAULT_IP));
        applicationArray.add(createSingleInstanceApplication("SERVICE-B", "server-2", "127.0.0.2"));

        return createRootNode(applicationArray);
    }

    /**
     * Creates Eureka structure with service on multiple hosts
     */
    public static ObjectNode createRootNodeWithServiceOnMultipleHosts(String serviceName) {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();
        ObjectNode app = createApplication(serviceName);

        ArrayNode instanceArray = NODE_FACTORY.arrayNode();
        instanceArray.add(createBasicInstanceNode("host-1", DEFAULT_IP, "instance-1"));
        instanceArray.add(createBasicInstanceNode("host-2", "127.0.0.2", "instance-2"));
        app.set("instance", instanceArray);

        applicationArray.add(app);
        return createRootNode(applicationArray);
    }

    public static ObjectNode createRootNodeWithMultipleInstances(String serviceName) {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();
        ObjectNode app = createApplication(serviceName);

        ArrayNode instanceArray = NODE_FACTORY.arrayNode();
        instanceArray.add(createBasicInstanceNode("host1", DEFAULT_IP, "instance-1"));
        instanceArray.add(createBasicInstanceNode("host2", "127.0.0.2", "instance-2"));
        app.set("instance", instanceArray);

        applicationArray.add(app);
        return createRootNode(applicationArray);
    }

    /**
     * Creates Eureka structure with empty service name
     */
    public static ObjectNode createRootNodeWithEmptyServiceName() {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();
        ObjectNode app = createApplication("");
        ArrayNode instanceArray = NODE_FACTORY.arrayNode();
        instanceArray.add(createBasicInstanceNode("localhost", DEFAULT_IP, "test-instance"));
        app.set("instance", instanceArray);
        applicationArray.add(app);

        return createRootNode(applicationArray);
    }

    public static ObjectNode createRootNodeWithNonArrayApplications() {
        ObjectNode root = NODE_FACTORY.objectNode();
        ObjectNode applications = NODE_FACTORY.objectNode();
        applications.put("application", "not-an-array");
        root.set("applications", applications);
        return root;
    }

    public static ObjectNode createRootNodeWithNonArrayInstances() {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();
        ObjectNode app = createApplication("TEST-SERVICE");
        app.put("instance", "not-an-array");
        applicationArray.add(app);

        return createRootNode(applicationArray);
    }

    public static ObjectNode createRootNodeWithInvalidInstances() {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();
        ObjectNode app = createApplication("TEST-SERVICE");
        ArrayNode instanceArray = NODE_FACTORY.arrayNode();
        instanceArray.add(createBasicInstanceNode("localhost", DEFAULT_IP, "test-instance"));
        app.set("instance", instanceArray);
        applicationArray.add(app);

        return createRootNode(applicationArray);
    }

    public static ObjectNode createRealEurekaResponseStructure() {
        ObjectNode root = NODE_FACTORY.objectNode();
        ObjectNode applications = NODE_FACTORY.objectNode();
        applications.put("versions__delta", "1");
        applications.put("apps__hashcode", "UP_1_");

        ArrayNode applicationArray = NODE_FACTORY.arrayNode();
        ObjectNode app = createApplication("CDI-EUREKA-SERVICE");

        ArrayNode instanceArray = NODE_FACTORY.arrayNode();
        ObjectNode instance = createRealEurekaInstance();
        instanceArray.add(instance);
        app.set("instance", instanceArray);
        applicationArray.add(app);

        applications.set("application", applicationArray);
        root.set("applications", applications);
        return root;
    }

    /**
     * Creates Eureka structure with no instances
     */
    public static ObjectNode createRootNodeWithNoInstances() {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();
        ObjectNode app = createApplication("TEST-SERVICE");
        // No instance array added
        applicationArray.add(app);

        return createRootNode(applicationArray);
    }

    /**
     * Creates Eureka structure with blank hostname
     */
    public static ObjectNode createRootNodeWithBlankHostname() {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();
        ObjectNode app = createApplication("TEST-SERVICE");
        ArrayNode instanceArray = NODE_FACTORY.arrayNode();
        instanceArray.add(createBasicInstanceNode("", DEFAULT_IP, "test-instance"));
        app.set("instance", instanceArray);
        applicationArray.add(app);

        return createRootNode(applicationArray);
    }

    public static ObjectNode createRootNodeWithBlankHostname(String serviceName, String ipAddr) {
        ArrayNode applicationArray = NODE_FACTORY.arrayNode();
        ObjectNode app = createApplication(serviceName);
        ArrayNode instanceArray = NODE_FACTORY.arrayNode();
        instanceArray.add(createBasicInstanceNode("", ipAddr, "test-instance"));
        app.set("instance", instanceArray);
        applicationArray.add(app);

        return createRootNode(applicationArray);
    }

    // ========== Private Helper Methods ==========

    private static ObjectNode createRootNode(ArrayNode applicationArray) {
        ObjectNode root = NODE_FACTORY.objectNode();
        ObjectNode applications = NODE_FACTORY.objectNode();
        applications.set("application", applicationArray);
        root.set("applications", applications);
        return root;
    }

    private static ObjectNode createApplication(String serviceName) {
        ObjectNode app = NODE_FACTORY.objectNode();
        app.put("name", serviceName);
        return app;
    }

    private static ObjectNode createSingleInstanceApplication(String serviceName, String hostname, String ipAddr) {
        ObjectNode app = createApplication(serviceName);
        ArrayNode instanceArray = NODE_FACTORY.arrayNode();
        instanceArray.add(createBasicInstanceNode(hostname, ipAddr, generateInstanceId(serviceName)));
        app.set("instance", instanceArray);
        return app;
    }

    private static String generateInstanceId(String serviceName) {
        return serviceName.toLowerCase() + "-" + System.currentTimeMillis();
    }

    /**
     * Creates basic instance node
     */
    public static ObjectNode createBasicInstanceNode(String hostname, String ipAddr, String instanceId) {
        ObjectNode instance = NODE_FACTORY.objectNode();
        instance.put("instanceId", instanceId);
        instance.put("hostName", hostname);
        instance.put("ipAddr", ipAddr);
        instance.put("status", DEFAULT_STATUS);
        instance.put("homePageUrl", String.format("http://%s:%d/", hostname, DEFAULT_PORT));
        instance.put("statusPageUrl", String.format("http://%s:%d/actuator/info", hostname, DEFAULT_PORT));
        instance.put("healthCheckUrl", String.format("http://%s:%d/actuator/health", hostname, DEFAULT_PORT));
        return instance;
    }

    private static ObjectNode createRealEurekaInstance() {
        ObjectNode instance = NODE_FACTORY.objectNode();
        instance.put("instanceId", "LAPTOP-LU3EDETB.mshome.net:cdi-eureka-service:8080");
        instance.put("hostName", "localhost");
        instance.put("app", "CDI-EUREKA-SERVICE");
        instance.put("ipAddr", "172.17.176.1");
        instance.put("status", DEFAULT_STATUS);
        instance.put("overriddenStatus", "UNKNOWN");

        // Port structure
        instance.set("port", createPortNode(DEFAULT_PORT, true));
        instance.set("securePort", createPortNode(DEFAULT_HTTPS_PORT, false));

        instance.put("homePageUrl", "http://localhost:8080/");
        instance.put("statusPageUrl", "http://localhost:8080/actuator/info");
        instance.put("healthCheckUrl", "http://localhost:8080/actuator/health");
        instance.put("isCoordinatingDiscoveryServer", "false");

        long currentTime = System.currentTimeMillis();
        instance.put("lastUpdatedTimestamp", String.valueOf(currentTime));
        instance.put("lastDirtyTimestamp", String.valueOf(currentTime - 1000));

        // LeaseInfo structure
        instance.set("leaseInfo", createLeaseInfoNode(currentTime));

        // Metadata structure
        ObjectNode metadata = NODE_FACTORY.objectNode();
        metadata.put("management.port", "8080");
        instance.set("metadata", metadata);

        return instance;
    }

    private static ObjectNode createPortNode(int port, boolean enabled) {
        ObjectNode portNode = NODE_FACTORY.objectNode();
        portNode.put("$", port);
        portNode.put("@enabled", String.valueOf(enabled));
        return portNode;
    }

    private static ObjectNode createLeaseInfoNode(long currentTime) {
        ObjectNode leaseInfo = NODE_FACTORY.objectNode();
        leaseInfo.put("renewalIntervalInSecs", 30);
        leaseInfo.put("durationInSecs", 90);
        leaseInfo.put("registrationTimestamp", currentTime);
        leaseInfo.put("lastRenewalTimestamp", currentTime);
        leaseInfo.put("evictionTimestamp", 0);
        leaseInfo.put("serviceUpTimestamp", currentTime);
        return leaseInfo;
    }

    /**
     * Creates mock BaseInstanceDetail for testing
     */
    public static BaseInstanceDetail createMockInstanceDetail() {
        long currentTime = System.currentTimeMillis();
        return new BaseInstanceDetail(
                DEFAULT_IP,
                DEFAULT_PORT,
                DEFAULT_HTTPS_PORT,
                "http://localhost:8080/actuator/health",
                "http://localhost:8080",
                "http://localhost:8080/actuator/info",
                DEFAULT_STATUS,
                currentTime,
                currentTime,
                false,
                new Metadata(null, null, null, null, null),
                new LeaseInfo(30, 90, currentTime, currentTime, 0, currentTime)
        );
    }
}
