package com.ecommerce.userservice.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
)
{
}
