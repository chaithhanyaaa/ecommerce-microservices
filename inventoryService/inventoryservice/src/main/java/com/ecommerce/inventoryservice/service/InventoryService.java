package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.request.CreateInventoryRequest;
import com.ecommerce.inventoryservice.dto.response.InventoryResponse;
import com.ecommerce.inventoryservice.dto.response.SizeStockResponse;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService
{
    private final InventoryRepository inventoryRepository;

    public void createInventory(CreateInventoryRequest request) {

        List<Inventory> inventories = request.getSizeStocks()
                .stream()
                .map(sizeStock -> Inventory.builder()
                        .productId(request.getProductId())
                        .size(sizeStock.getSize())
                        .stock(sizeStock.getStock())
                        .build())
                .toList();

        inventoryRepository.saveAll(inventories);
    }

    public InventoryResponse getInventory(Long productId) {

        List<Inventory> inventories = inventoryRepository.findByProductId(productId);

        List<SizeStockResponse> sizeStocks = inventories.stream()
                .map(inventory -> SizeStockResponse.builder()
                        .size(inventory.getSize())
                        .stock(inventory.getStock())
                        .build())
                .toList();

        return InventoryResponse.builder()
                .productId(productId)
                .sizeStocks(sizeStocks)
                .build();
    }
}
