package com.dafreurekadetails.exception;

import com.dafreurekadetails.dto.response.ReturnCode;

public class InstanceMappingException extends ApiException {
    public InstanceMappingException(String message, Throwable cause) {
        super(
                ReturnCode.INVALID_REQUEST,
                message,
                cause
        );
    }
    public InstanceMappingException(String message) {
        super(
                ReturnCode.INVALID_REQUEST,
                message
        );
    }
}
