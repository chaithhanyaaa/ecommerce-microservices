package com.ecommerce.userservice.dto;

import java.time.LocalDateTime;

public record ApiResponse(
        LocalDateTime timestamp,
        int status,
        String message
) {
}