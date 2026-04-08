package com.servio.backend.shared.web;

import com.servio.backend.shared.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NotNull ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST, "Validation error", errors));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<@NotNull ApiResponse<Void>> handleAppException(AppException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getStatus(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NotNull ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));
    }
}