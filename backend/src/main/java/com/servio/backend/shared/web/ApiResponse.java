package com.servio.backend.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int status;
    private final String error;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    private ApiResponse(HttpStatus httpStatus, String message, T data, boolean isError) {
        this.status = httpStatus.value();
        this.error = isError ? httpStatus.getReasonPhrase() : null;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(HttpStatus.OK, "ok", data, false);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(HttpStatus.OK, message, data, false);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(HttpStatus.CREATED, "created", data, false);
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(HttpStatus.NO_CONTENT, "no content", null, false);
    }

    public static <T> ApiResponse<T> error(HttpStatus httpStatus, String message) {
        return new ApiResponse<>(httpStatus, message, null, true);
    }

    public static <T> ApiResponse<T> error(HttpStatus httpStatus, String message, T data) {
        return new ApiResponse<>(httpStatus, message, data, true);
    }
}