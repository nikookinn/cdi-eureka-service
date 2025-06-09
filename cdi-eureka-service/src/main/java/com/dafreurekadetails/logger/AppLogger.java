package com.dafreurekadetails.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class AppLogger {
    private final Logger logger;

    public AppLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static AppLogger getLogger(Class<?> clazz) {
        return new AppLogger(clazz);
    }

    private String formatMessage(String message) {
        String transactionId = MDC.get("transactionId");
        if (transactionId != null) {
            return "[TxID: " + transactionId + "] " + message;
        }
        return message;
    }

    public void info(String message) {
        logger.info(formatMessage(message));
    }

    public void info(String message, Object... args) {
        logger.info(formatMessage(message), args);
    }

    public void debug(String message) {
        logger.debug(formatMessage(message));
    }

    public void debug(String message, Object... args) {
        logger.debug(formatMessage(message), args);
    }

    public void warn(String message) {
        logger.warn(formatMessage(message));
    }

    public void warn(String message, Object... args) {
        logger.warn(formatMessage(message), args);
    }

    public void error(String message) {
        logger.error(formatMessage(message));
    }

    public void error(String message, Throwable throwable) {
        logger.error(formatMessage(message), throwable);
    }

    public void error(String message, Object... args) {
        logger.error(formatMessage(message), args);
    }

    public void trace(String message) {
        logger.trace(formatMessage(message));
    }

    public void trace(String message, Object... args) {
        logger.trace(formatMessage(message), args);
    }
}
