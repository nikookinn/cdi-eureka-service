package com.dafreurekadetails.service;

import com.dafreurekadetails.dto.response.ReturnCode;
import com.dafreurekadetails.exception.EurekaTimeoutException;
import com.dafreurekadetails.exception.ServiceUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class EurekaClientHelperTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private EurekaClientHelper eurekaClientHelper;
    private JsonNode mockJsonNode;
    private String eurekaURL;
    private String expectedUrl;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        String mockJson = "{\"applications\":{\"application\":[{\"name\":\"TEST-SERVICE\"}]}}";
        mockJsonNode = objectMapper.readTree(mockJson);
        eurekaURL = "http://localhost:8761";
        expectedUrl = "http://localhost:8761/apps";
    }
    @Test
    public void getEurekaApps_ShouldReturnJsonNode_WhenSuccessful(){

        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(mockJsonNode, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(expectedUrl), eq(JsonNode.class)))
                .thenReturn(mockResponse);

        JsonNode result = eurekaClientHelper.getEurekaApps(eurekaURL);

        assertNotNull(result);
        Assertions.assertEquals(mockJsonNode,result);

        verify(restTemplate).getForEntity(eq(expectedUrl), eq(JsonNode.class));
    }
    @Test
    void getEurekaApps_ShouldThrowEurekaTimeoutException_WhenSocketTimeoutException() {
        // Given
        String eurekaURL = "http://localhost:8761";
        String expectedURL = "http://localhost:8761/apps";
        SocketTimeoutException socketTimeoutException = new SocketTimeoutException("Connection timed out");
        ResourceAccessException resourceAccessException = new ResourceAccessException("Timeout", socketTimeoutException);

        when(restTemplate.getForEntity(eq(expectedURL), eq(JsonNode.class)))
                .thenThrow(resourceAccessException);

        // When & Then
        EurekaTimeoutException exception = assertThrows(EurekaTimeoutException.class,
                () -> eurekaClientHelper.getEurekaApps(eurekaURL));

        assertTrue(exception.getMessage().contains("Connection timeout to Eureka server"));
        assertTrue(exception.getMessage().contains(expectedURL));
        assertEquals(resourceAccessException, exception.getCause());
    }
    @Test
    void getEurekaApps_ShouldThrowServiceUnavailableExceptionWithServiceDown_WhenConnectException() {
        String eurekaURL = "http://localhost:8761";
        String expectedURL = "http://localhost:8761/apps";
        ConnectException connectException = new ConnectException("Connection refused");
        ResourceAccessException resourceAccessException = new ResourceAccessException("Connection failed", connectException);

        when(restTemplate.getForEntity(eq(expectedURL), eq(JsonNode.class)))
                .thenThrow(resourceAccessException);

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> eurekaClientHelper.getEurekaApps(eurekaURL));

        assertEquals(ReturnCode.SERVICE_DOWN, exception.returnCode());
        assertTrue(exception.getMessage().contains("Cannot connect to Eureka server"));
        assertTrue(exception.getMessage().contains(expectedURL));
        assertEquals(resourceAccessException, exception.getCause());
    }
    @Test
    void getEurekaApps_ShouldThrowServiceUnavailableExceptionWithInvalidHost_WhenUnknownHostException() {
        String eurekaURL = "http://invalid-host:8761";
        String expectedURL = "http://invalid-host:8761/apps";
        UnknownHostException unknownHostException = new UnknownHostException("Unknown host");
        ResourceAccessException resourceAccessException = new ResourceAccessException("Host not found", unknownHostException);

        when(restTemplate.getForEntity(eq(expectedURL), eq(JsonNode.class)))
                .thenThrow(resourceAccessException);

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> eurekaClientHelper.getEurekaApps(eurekaURL));

        assertEquals(ReturnCode.INVALID_HOST, exception.returnCode());
        assertTrue(exception.getMessage().contains("Unknown host in Eureka URL"));
        assertTrue(exception.getMessage().contains(expectedURL));
        assertEquals(resourceAccessException, exception.getCause());
    }
    @Test
    void getEurekaApps_ShouldThrowServiceUnavailableExceptionWithUnknown_WhenResourceAccessExceptionWithOtherCause() {
        String eurekaURL = "http://localhost:8761";
        String expectedURL = "http://localhost:8761/apps";
        IOException otherException = new IOException("Some other error");
        ResourceAccessException resourceAccessException = new ResourceAccessException("Other error", otherException);

        when(restTemplate.getForEntity(eq(expectedURL), eq(JsonNode.class)))
                .thenThrow(resourceAccessException);

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> eurekaClientHelper.getEurekaApps(eurekaURL));

        assertEquals(ReturnCode.UNKNOWN, exception.returnCode());
        assertTrue(exception.getMessage().contains("Cannot connect to Eureka server"));
        assertTrue(exception.getMessage().contains(expectedURL));
        assertEquals(resourceAccessException, exception.getCause());
    }
    @Test
    void getEurekaApps_ShouldThrowServiceUnavailableExceptionWithServiceUnavailable_WhenRestClientException() {
        String eurekaURL = "http://localhost:8761";
        String expectedURL = "http://localhost:8761/apps";
        RestClientException restClientException = new RestClientException("REST client error");

        when(restTemplate.getForEntity(eq(expectedURL), eq(JsonNode.class)))
                .thenThrow(restClientException);

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> eurekaClientHelper.getEurekaApps(eurekaURL));

        assertEquals(ReturnCode.SERVICE_UNAVAILABLE, exception.returnCode());
        assertTrue(exception.getMessage().contains("Service is currently not responding"));
        assertTrue(exception.getMessage().contains(expectedURL));
        assertEquals(restClientException, exception.getCause());
    }
    @Test
    void getEurekaApps_ShouldThrowServiceUnavailableExceptionWithUnknown_WhenUnexpectedException() {
        String eurekaURL = "http://localhost:8761";
        String expectedURL = "http://localhost:8761/apps";
        RuntimeException unexpectedException = new RuntimeException("Unexpected error");

        when(restTemplate.getForEntity(eq(expectedURL), eq(JsonNode.class)))
                .thenThrow(unexpectedException);

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> eurekaClientHelper.getEurekaApps(eurekaURL));

        assertEquals(ReturnCode.UNKNOWN, exception.returnCode());
        assertTrue(exception.getMessage().contains("Unexpected error connecting to Eureka"));
        assertTrue(exception.getMessage().contains(expectedURL));
        assertEquals(unexpectedException, exception.getCause());
    }
    @Test
    void getEurekaApps_ShouldReturnNull_WhenNullResponse() {
        String eurekaURL = "http://localhost:8761";
        String expectedURL = "http://localhost:8761/apps";
        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.getForEntity(eq(expectedURL), eq(JsonNode.class)))
                .thenReturn(mockResponse);

        JsonNode result = eurekaClientHelper.getEurekaApps(eurekaURL);

        assertNull(result);
        verify(restTemplate).getForEntity(eq(expectedURL), eq(JsonNode.class));
    }
    @Test
    void getEurekaApps_ShouldVerifyCorrectURLConstruction() {
        testURLConstruction("http://localhost:8761", "http://localhost:8761/apps");
        testURLConstruction("http://localhost:8761/", "http://localhost:8761/apps");
        testURLConstruction("https://eureka.example.com", "https://eureka.example.com/apps");
        testURLConstruction("https://eureka.example.com/", "https://eureka.example.com/apps");
    }
    private void testURLConstruction(String inputURL, String expectedURL) {
        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(mockJsonNode, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(expectedURL), eq(JsonNode.class)))
                .thenReturn(mockResponse);

        eurekaClientHelper.getEurekaApps(inputURL);

        verify(restTemplate).getForEntity(eq(expectedURL), eq(JsonNode.class));
        reset(restTemplate);
    }
}
