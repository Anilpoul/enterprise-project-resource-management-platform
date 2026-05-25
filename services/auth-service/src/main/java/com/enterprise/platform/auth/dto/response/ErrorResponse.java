package com.enterprise.platform.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {

    private boolean success;

    private String message;

    private int status;

    private LocalDateTime timestamp;

    private Map<String, String> validationErrors;

}