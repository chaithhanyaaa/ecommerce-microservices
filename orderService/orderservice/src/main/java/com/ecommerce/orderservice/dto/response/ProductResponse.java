package com.ecommerce.orderservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;

    private Long sellerId;

    private String name;

    private String description;

    private String brand;

    private String category;

    private String gender;

    private BigDecimal actualPrice;

    private BigDecimal offerPrice;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<String> availableSizes;
}