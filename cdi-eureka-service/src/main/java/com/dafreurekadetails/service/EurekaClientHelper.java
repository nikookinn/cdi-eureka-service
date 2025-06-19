package com.dafreurekadetails.service;

import com.dafreurekadetails.dto.response.ReturnCode;
import com.dafreurekadetails.exception.EurekaTimeoutException;
import com.dafreurekadetails.exception.ServiceUnavailableException;
import com.dafreurekadetails.logger.AppLogger;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
/**
 * EurekaClientHelper is a utility component responsible for communicating with
 * a Eureka server and retrieving application registration data.
 * <p>
 * It uses {@link RestTemplate} to make REST calls to the Eureka API and
 * provides a parsed {@link JsonNode} representation of the /apps endpoint.
 * <p>
 * This class centralizes all network-related error handling and maps known issues
 * like timeouts, host resolution failures, or connection errors to domain-specific exceptions.
 */
@Component
public class EurekaClientHelper {

    private static final AppLogger APP_LOGGER = AppLogger.getLogger(EurekaClientHelper.class);
    private final RestTemplate restTemplate;

    public EurekaClientHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    /**
     * Calls the /apps endpoint of the Eureka server to fetch all registered applications.
     *
     * @param eurekaURL the base URL of the Eureka server (e.g., <a href="http://localhost:8761/eureka"/>)
     * @return a {@link JsonNode} representing the JSON response from the Eureka server
     * @throws EurekaTimeoutException        if a timeout occurs while connecting
     * @throws ServiceUnavailableException   if the Eureka server is down, unreachable, or returns an error
     */
    public JsonNode getEurekaApps(String eurekaURL) {

        String url = buildAppsURL(eurekaURL);
        APP_LOGGER.debug("Fetching Eureka apps from: {}", url);

        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

            APP_LOGGER.debug("Successfully fetched Eureka apps from: {}", url);
            return response.getBody();

        } catch (ResourceAccessException ex) {
            APP_LOGGER.error("Resource access error when connecting to Eureka: {}", ex.getMessage());

            if (ex.getCause() instanceof SocketTimeoutException) {
                throw new EurekaTimeoutException("Connection timeout to Eureka server: " + url, ex);
            } else if (ex.getCause() instanceof ConnectException) {
                throw new ServiceUnavailableException(ReturnCode.SERVICE_DOWN, "Cannot connect to Eureka server: " + url, ex);
            } else if (ex.getCause() instanceof UnknownHostException) {
                throw new ServiceUnavailableException(ReturnCode.INVALID_HOST, "Unknown host in Eureka URL: " + url, ex);
            } else {
                throw new ServiceUnavailableException(ReturnCode.UNKNOWN, "Cannot connect to Eureka server: " + url, ex);
            }

        } catch (RestClientException ex) {
            APP_LOGGER.error("REST client error when connecting to Eureka: {}", ex.getMessage());
            throw new ServiceUnavailableException(ReturnCode.SERVICE_UNAVAILABLE, "Service is currently not responding: " + url, ex);

        } catch (Exception ex) {
            APP_LOGGER.error("Unexpected error when connecting to Eureka: {}", ex.getMessage(), ex);
            throw new ServiceUnavailableException(ReturnCode.UNKNOWN, "Unexpected error connecting to Eureka: " + url, ex);
        }
    }

    /**
     * Builds the complete /apps endpoint URL from the base Eureka URL.
     * <p>
     * Ensures no trailing slash before appending "/apps".
     *
     * @param eurekaURL the base Eureka server URL
     * @return the full URL to call for retrieving registered applications
     */
    private String buildAppsURL(String eurekaURL) {
        String baseURL = eurekaURL.endsWith("/") ? eurekaURL.substring(0, eurekaURL.length() - 1) : eurekaURL;
        return baseURL + "/apps";
    }
}
