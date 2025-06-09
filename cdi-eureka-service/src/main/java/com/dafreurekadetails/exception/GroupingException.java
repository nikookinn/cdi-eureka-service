package com.dafreurekadetails.exception;

import com.dafreurekadetails.dto.response.ReturnCode;

public class GroupingException extends ApiException {
    public GroupingException(String message, Throwable throwable) {
        super(
                ReturnCode.UNKNOWN,
                message,
                throwable);
    }

    public GroupingException(String message) {
        super(
                ReturnCode.UNKNOWN,
                message);

    }
}
