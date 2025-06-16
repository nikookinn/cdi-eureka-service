package com.dafreurekadetails.service;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.response.EurekaQueryResponse;
import com.dafreurekadetails.dto.response.ReturnCode;
import com.dafreurekadetails.exception.ApiException;
import com.dafreurekadetails.exception.GroupingException;
import com.dafreurekadetails.exception.InvalidRequestException;
import com.dafreurekadetails.logger.AppLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
@Service
public class EurekaQueryService {

    private static final AppLogger APP_LOGGER = AppLogger.getLogger(EurekaQueryResponse.class);

    private final EurekaService eurekaService;

    public EurekaQueryService(EurekaService eurekaService) {
        this.eurekaService = eurekaService;
    }

    public EurekaQueryResponse<GroupedResult> handleQuery(String groupBy, String eurekaURL) {
        long startTime = System.currentTimeMillis();
        String transactionId = (String) RequestContextHolder.getRequestAttributes()
                .getAttribute("transactionId", RequestAttributes.SCOPE_REQUEST);

        APP_LOGGER.info("Starting Eureka query - GroupBy: {}, URL: {}", groupBy, eurekaURL);

        try {
            validateEurekaURL(eurekaURL);

            GroupedResult data = eurekaService.group(groupBy, eurekaURL);
            double elapsedTime = calculateElapsedTime(startTime);

            APP_LOGGER.info("Eureka query completed successfully in {} ms", elapsedTime);

            return EurekaQueryResponse.from(ReturnCode.SUCCESS,ReturnCode.SUCCESS.getMessage(), transactionId, elapsedTime, data);

        }catch (ApiException ex) {
            double elapsedTime = calculateElapsedTime(startTime);
            APP_LOGGER.warn("Handled API error after {} ms: {}", elapsedTime, ex.getMessage());
            throw ex;

        } catch (Exception ex) {
            double elapsedTime = calculateElapsedTime(startTime);
            APP_LOGGER.error("Eureka query failed after {} ms: {}" ,elapsedTime, ex.getMessage(), ex);
            throw new GroupingException("Eureka query [" + transactionId + "] failed after " + (long) elapsedTime + " ms", ex);

        }
    }

    private void validateEurekaURL(String url) {
        if (url == null) {
            throw new InvalidRequestException("Invalid URL format: null");
        }
        try {
            URI uri = new URI(url);

            if (uri.getScheme() == null || !(uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"))) {
                throw new InvalidRequestException("Eureka URL must start with http:// or https://");
            }

            if (uri.getHost() == null) {
                throw new InvalidRequestException("Eureka URL must contain a valid host.");
            }

            if (!uri.getPath().contains("/eureka")) {
                throw new InvalidRequestException("Eureka URL must contain the '/eureka' path segment.");
            }

        } catch (URISyntaxException e) {
            throw new InvalidRequestException("Invalid URL format: " + url);
        }
    }


    private double calculateElapsedTime(long startTime) {
        return (System.currentTimeMillis() - startTime);
    }
}
