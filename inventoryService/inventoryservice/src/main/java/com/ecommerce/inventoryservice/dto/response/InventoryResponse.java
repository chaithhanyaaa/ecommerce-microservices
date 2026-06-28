package com.ecommerce.inventoryservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long productId;

    private List<SizeStockResponse> sizeStocks;
}