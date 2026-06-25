package com.ecommerce.userservice.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        Map<String,String> errors
) {
}