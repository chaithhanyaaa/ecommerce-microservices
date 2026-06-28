package com.ecommerce.inventoryservice.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeStockResponse {

    private String size;

    private Integer stock;
}