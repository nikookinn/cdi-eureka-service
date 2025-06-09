package com.dafreurekadetails.exception;

import com.dafreurekadetails.dto.response.ReturnCode;

public class ServiceUnavailableException extends ApiException{
    public ServiceUnavailableException(String message) {
        super(ReturnCode.SERVICE_DOWN, message);
    }

    public ServiceUnavailableException(ReturnCode returnCode,String message, Throwable cause) {
        super(
                returnCode,
                message,
                cause);
    }
}
