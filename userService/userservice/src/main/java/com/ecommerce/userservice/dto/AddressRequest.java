package com.ecommerce.userservice.dto;
import jakarta.validation.constraints.NotBlank;

public record AddressRequest(

        @NotBlank(message = "House number is required")
        String houseNo,

        @NotBlank(message = "Street is required")
        String street,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "State is required")
        String state,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Pincode is required")
        String pincode
) {}
