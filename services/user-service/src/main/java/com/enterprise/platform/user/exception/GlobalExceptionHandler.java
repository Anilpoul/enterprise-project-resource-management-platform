package com.enterprise.platform.user.exception;

import com.enterprise.platform.user.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleResourceNotFound(
            ResourceNotFoundException ex
    ) {

        log.warn(
                "Resource not found: {}",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleBadRequest(
            BadRequestException ex
    ) {

        log.warn(
                "Bad request: {}",
                ex.getMessage()
        );

        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message(ex.getMessage())
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>>
    handleValidationException(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> errors =
                new HashMap<>();

        for (FieldError error :
                ex.getBindingResult()
                        .getFieldErrors()) {

            errors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        log.warn(
                "Validation failed: {}",
                errors
        );

        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponse
                                .<Map<String, String>>builder()
                                .success(false)
                                .message(
                                        "Validation failed"
                                )
                                .data(errors)
                                .timestamp(
                                        LocalDateTime.now()
                                )
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>>
    handleGenericException(
            Exception ex
    ) {

        log.error(
                "Unhandled exception",
                ex
        );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message(
                                        "Internal server error"
                                )
                                .timestamp(
                                        LocalDateTime.now()
                                )
                                .build()
                );
    }
}