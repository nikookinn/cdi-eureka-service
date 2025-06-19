package com.dafreurekadetails.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;
/**
 * Interceptor that generates a unique transaction ID for each incoming HTTP request.
 * The transaction ID is stored in the request attributes and in the MDC for logging.
 */
public class TransactionIdInterceptor implements HandlerInterceptor {
    /**
     * Generates a UUID as a transaction ID and stores it in both the request and MDC.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String transactionId = UUID.randomUUID().toString();
        request.setAttribute("transactionId", transactionId);
        MDC.put("transactionId", transactionId);
        return true;
    }
    /**
     * Cleans up the MDC context after request completion.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove("transactionId");
    }
}
