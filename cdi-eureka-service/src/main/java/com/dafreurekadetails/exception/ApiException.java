package com.dafreurekadetails.exception;

import com.dafreurekadetails.dto.response.ReturnCode;

public abstract class ApiException extends RuntimeException {

    private final ReturnCode returnCode;

    public ApiException(ReturnCode returnCode, String message) {
        super(message);
        this.returnCode = returnCode;
    }

    public ApiException(ReturnCode returnCode, String message, Throwable cause) {
        super(message, cause);
        this.returnCode = returnCode;
    }

    public ReturnCode returnCode() {
        return returnCode;
    }
}
