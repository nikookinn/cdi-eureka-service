package com.dafreurekadetails.dto.response;

import org.springframework.http.HttpStatus;

public enum ReturnCode {
    SUCCESS("SUCCESS", "Request completed successfully.", HttpStatus.OK),
    INVALID_REQUEST("INVALID_REQUEST", "The query is badly formatted (e.g., missing fields).", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("INVALID_TOKEN", "The provided JWT token is invalid or expired.", HttpStatus.BAD_REQUEST),
    AUTH_REQUIRED("AUTH_REQUIRED", "Authentication to server failed.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("ACCESS_DENIED", "Credentials were valid but action was not permitted.", HttpStatus.FORBIDDEN),
    SERVICE_DOWN("SERVICE_DOWN", "The system is currently unavailable.", HttpStatus.BAD_GATEWAY),
    TIMEOUT("TIMEOUT", "The operation took too long and was aborted.", HttpStatus.GATEWAY_TIMEOUT),
    UNKNOWN("UNKNOWN", "An unexpected or unclassified internal error happened.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_HOST("INVALID_HOST", "The provided hostname could not be resolved.", HttpStatus.BAD_REQUEST),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "The requested service is currently not responding. Try again in a few moments.",HttpStatus.SERVICE_UNAVAILABLE);
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ReturnCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int status() {
        return httpStatus.value();
    }
}
