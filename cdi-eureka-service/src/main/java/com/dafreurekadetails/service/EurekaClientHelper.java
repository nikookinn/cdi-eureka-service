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

@Component
public class EurekaClientHelper {

    private static final AppLogger APP_LOGGER = AppLogger.getLogger(EurekaClientHelper.class);
    private final RestTemplate restTemplate;

    public EurekaClientHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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


    private String buildAppsURL(String eurekaURL) {
        String baseURL = eurekaURL.endsWith("/") ? eurekaURL.substring(0, eurekaURL.length() - 1) : eurekaURL;
        return baseURL + "/apps";
    }
}
