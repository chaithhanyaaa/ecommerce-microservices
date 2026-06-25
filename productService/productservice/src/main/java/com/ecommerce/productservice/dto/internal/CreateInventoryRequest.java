package com.ecommerce.productservice.dto.internal;

import com.ecommerce.productservice.dto.request.SizeStockRequest;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInventoryRequest {

    private Long productId;

    private List<SizeStockRequest> sizeStocks;

}