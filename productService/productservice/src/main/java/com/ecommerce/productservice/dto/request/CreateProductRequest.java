package com.ecommerce.productservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotNull(message = "Actual price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal actualPrice;

    @NotNull(message = "Offer price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal offerPrice;

    @Valid
    @NotEmpty(message = "At least one size must be provided")
    private List<SizeStockRequest> sizeStocks;
}