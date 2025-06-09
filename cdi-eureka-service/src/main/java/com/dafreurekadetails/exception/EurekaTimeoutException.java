package com.dafreurekadetails.exception;

import com.dafreurekadetails.dto.response.ReturnCode;

public class EurekaTimeoutException extends ApiException {

    public EurekaTimeoutException(String message, Throwable cause) {
        super(
                ReturnCode.TIMEOUT,
                message,
                cause);
    }
}
