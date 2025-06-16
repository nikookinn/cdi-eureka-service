package com.dafreurekadetails.service;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.ServiceResult;
import com.dafreurekadetails.dto.response.EurekaQueryResponse;
import com.dafreurekadetails.dto.response.ReturnCode;
import com.dafreurekadetails.exception.ApiException;
import com.dafreurekadetails.exception.GroupingException;
import com.dafreurekadetails.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EurekaQueryServiceTest {

    @Mock
    private EurekaService eurekaService;

    @Mock
    private RequestAttributes requestAttributes;

    @Mock
    private ServiceResult serviceResult;

    @InjectMocks
    private EurekaQueryService eurekaQueryService;

    private static final String VALID_EUREKA_URL = "http://localhost:8761/eureka/apps";
    private static final String HTTPS_EUREKA_URL = "https://localhost:8761/eureka/apps";
    private static final String TRANSACTION_ID = "test-transaction-123";
    private static final String GROUP_BY = "services";

    @BeforeEach
    void setUp() {
        when(requestAttributes.getAttribute("transactionId", RequestAttributes.SCOPE_REQUEST))
                .thenReturn(TRANSACTION_ID);
    }

    @Test
    void handleQuery_ShouldReturnSuccessResponse_WhenValidInputProvided() {
        when(eurekaService.group(GROUP_BY, VALID_EUREKA_URL)).thenReturn(serviceResult);

        EurekaQueryResponse<? extends GroupedResult> response = callHandleQueryWithMockedContext();

        assertNotNull(response);
        assertEquals(ReturnCode.SUCCESS.toString(), response.returnCode());
        assertEquals(ReturnCode.SUCCESS.getMessage(), response.message());
        assertEquals(TRANSACTION_ID, response.transactionID());
        assertEquals(serviceResult, response.data());
        assertTrue(response.elapsedTime() >= 0);
        verify(eurekaService).group(GROUP_BY, VALID_EUREKA_URL);
    }

    @Test
    void handleQuery_ShouldThrowApiException_WhenEurekaServiceThrowsApiException() {
        ApiException apiException = mock(ApiException.class);
        when(apiException.getMessage()).thenReturn("API Error");
        when(eurekaService.group(GROUP_BY, VALID_EUREKA_URL)).thenThrow(apiException);

        try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            ApiException thrown = assertThrows(ApiException.class, () ->
                    eurekaQueryService.handleQuery(GROUP_BY, VALID_EUREKA_URL));

            assertEquals("API Error", thrown.getMessage());
        }
        verify(eurekaService).group(GROUP_BY, VALID_EUREKA_URL);
    }

    @Test
    void handleQuery_ShouldThrowGroupingException_WhenEurekaServiceThrowsUnexpectedException() {
        RuntimeException runtimeException = new RuntimeException("Unexpected error");
        when(eurekaService.group(GROUP_BY, VALID_EUREKA_URL)).thenThrow(runtimeException);

        try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            GroupingException thrown = assertThrows(GroupingException.class, () ->
                    eurekaQueryService.handleQuery(GROUP_BY, VALID_EUREKA_URL));

            assertTrue(thrown.getMessage().contains("Eureka query [" + TRANSACTION_ID + "] failed"));
            assertTrue(thrown.getMessage().contains("ms"));
            assertEquals(runtimeException, thrown.getCause());
        }
        verify(eurekaService).group(GROUP_BY, VALID_EUREKA_URL);
    }

    @Test
    void handleQuery_ShouldThrowInvalidRequestException_WhenUrlIsNull() {
        testInvalidUrl(null, "Invalid URL format: null");
    }

    @Test
    void handleQuery_ShouldThrowInvalidRequestException_WhenUrlHasNoScheme() {
        testInvalidUrl("localhost:8761/eureka/apps", "Eureka URL must start with http:// or https://");
    }

    @Test
    void handleQuery_ShouldThrowInvalidRequestException_WhenUrlHasInvalidScheme() {
        testInvalidUrl("ftp://localhost:8761/eureka/apps", "Eureka URL must start with http:// or https://");
    }

    @Test
    void handleQuery_ShouldThrowInvalidRequestException_WhenUrlHasNoHost() {
        testInvalidUrl("http:///eureka/apps", "Eureka URL must contain a valid host.");
    }

    @Test
    void handleQuery_ShouldThrowInvalidRequestException_WhenUrlDoesNotContainEurekaPath() {
        testInvalidUrl("http://localhost:8761/admin/apps", "Eureka URL must contain the '/eureka' path segment.");
    }

    @Test
    void handleQuery_ShouldThrowInvalidRequestException_WhenUrlIsMalformed() {
        String malformedUrl = "http://[invalid-url";
        testInvalidUrl(malformedUrl, "Invalid URL format: " + malformedUrl);
    }

    @Test
    void handleQuery_ShouldAcceptHttpsUrls() {
        when(eurekaService.group(GROUP_BY, HTTPS_EUREKA_URL)).thenReturn(serviceResult);

        try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            EurekaQueryResponse<? extends GroupedResult> response =
                    eurekaQueryService.handleQuery(GROUP_BY, HTTPS_EUREKA_URL);

            assertNotNull(response);
            assertEquals(ReturnCode.SUCCESS.toString(), response.returnCode());
            verify(eurekaService).group(GROUP_BY, HTTPS_EUREKA_URL);
        }
    }

    @Test
    void handleQuery_ShouldMeasureElapsedTime() {
        when(eurekaService.group(GROUP_BY, VALID_EUREKA_URL))
                .thenAnswer(invocation -> {
                    Thread.sleep(10);
                    return serviceResult;
                });

        try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            EurekaQueryResponse<? extends GroupedResult> response =
                    eurekaQueryService.handleQuery(GROUP_BY, VALID_EUREKA_URL);

            assertTrue(response.elapsedTime() >= 10.0);
        }
    }

    private EurekaQueryResponse<? extends GroupedResult> callHandleQueryWithMockedContext() {
        try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            return eurekaQueryService.handleQuery(GROUP_BY, VALID_EUREKA_URL);
        }
    }

    private void testInvalidUrl(String invalidUrl, String expectedMessage) {
        try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            InvalidRequestException thrown = assertThrows(InvalidRequestException.class, () ->
                    eurekaQueryService.handleQuery(GROUP_BY, invalidUrl));

            assertEquals(expectedMessage, thrown.getMessage());
        }
        verify(eurekaService, never()).group(anyString(), anyString());
    }
}