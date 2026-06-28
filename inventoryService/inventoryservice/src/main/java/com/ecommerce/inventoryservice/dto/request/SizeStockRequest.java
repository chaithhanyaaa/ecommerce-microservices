package com.ecommerce.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeStockRequest {

    @NotBlank
    private String size;

    @NotNull
    @Min(0)
    private Integer stock;
}