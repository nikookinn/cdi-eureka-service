package com.dafreurekadetails.exception;

import com.dafreurekadetails.dto.GroupedResult;
import com.dafreurekadetails.dto.response.EurekaQueryResponse;
import com.dafreurekadetails.dto.response.ReturnCode;
import com.dafreurekadetails.logger.AppLogger;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;


@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<EurekaQueryResponse<GroupedResult>> handleApi(ApiException ex) {
        String transactionId = getTransactionId();
        return ResponseEntity
                .status(ex.returnCode().getHttpStatus())
                .body(EurekaQueryResponse.from(
                        ex.returnCode(),
                        ex.getMessage(),
                        transactionId,
                        0.0,
                        null
                ));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EurekaQueryResponse<GroupedResult>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String transactionId = getTransactionId();
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                sb.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );

        EurekaQueryResponse<GroupedResult> response = EurekaQueryResponse.from(
                ReturnCode.INVALID_REQUEST,
                sb.toString(),
                transactionId,
                0,
                null
        );

        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<EurekaQueryResponse<GroupedResult>> handleMalformedJson(HttpMessageNotReadableException ex) {
        String transactionId = getTransactionId();

        EurekaQueryResponse<GroupedResult> response = EurekaQueryResponse.from(
                ReturnCode.INVALID_REQUEST,
                "Malformed JSON request body",
                transactionId,
                0,
                null
        );

        return ResponseEntity.badRequest().body(response);
    }
    private String getTransactionId() {
        String transactionId = MDC.get("transactionId");
        return transactionId != null ? transactionId : UUID.randomUUID().toString();
    }
}
