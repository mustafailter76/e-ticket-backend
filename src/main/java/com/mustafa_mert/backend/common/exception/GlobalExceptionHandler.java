package com.mustafa_mert.backend.common.exception;

import com.mustafa_mert.backend.common.response.RootEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Catches custom exceptions that extend BaseException
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<RootEntity<?>> handleBaseException(BaseException ex, WebRequest request) {
        log.error("BaseException: {}", ex.getMessage());
        int status = ex.getErrorMessage().getMessageType().getStatus();
        return ResponseEntity
                .status(status)
                .body(RootEntity.error(ex.getMessage(), status));
    }

    // Catches validation errors and formats them into a structured response
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RootEntity<?>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors
                    .computeIfAbsent(fieldError.getField(), key -> new ArrayList<>())
                    .add(fieldError.getDefaultMessage());
        }

        return ResponseEntity
                .badRequest()
                .body(RootEntity.validationError(errors));
    }

    // Catches any other unhandled exceptions and returns a generic error response
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RootEntity<?>> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RootEntity.error(
                        MessageType.GENERAL_EXCEPTION.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
    }
}