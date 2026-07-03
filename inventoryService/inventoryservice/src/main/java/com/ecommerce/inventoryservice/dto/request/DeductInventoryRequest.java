package com.ecommerce.inventoryservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;


@Data
@Builder
public class DeductInventoryRequest {

    @NotNull
    private Long productId;

    @NotBlank
    private String size;

    @NotNull
    @Min(1)
    private Integer quantity;
}