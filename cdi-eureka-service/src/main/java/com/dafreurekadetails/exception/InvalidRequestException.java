package com.dafreurekadetails.exception;

import com.dafreurekadetails.dto.response.ReturnCode;

public class InvalidRequestException extends ApiException {
    public InvalidRequestException(String message) {
        super(ReturnCode.INVALID_REQUEST, message);
    }

    public InvalidRequestException(String message, Throwable throwable) {
        super(
                ReturnCode.INVALID_REQUEST,
                message != null ? message : ReturnCode.INVALID_REQUEST.getMessage(),
                throwable);
    }
}
