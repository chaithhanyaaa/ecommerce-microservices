package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.request.CreateInventoryRequest;
import com.ecommerce.inventoryservice.dto.request.DeductInventoryRequest;
import com.ecommerce.inventoryservice.dto.response.InventoryResponse;
import com.ecommerce.inventoryservice.dto.response.SizeStockResponse;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.exception.InsufficientStockException;
import com.ecommerce.inventoryservice.exception.InventoryNotFoundException;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService
{
    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Integer> redisTemplate;
    private final RedisScript<Long> deductStockScript;

    public void createInventory(CreateInventoryRequest request) {

        List<Inventory> inventories = request.getSizeStocks()
                .stream()
                .map(sizeStock -> Inventory.builder()
                        .productId(request.getProductId())
                        .size(sizeStock.getSize())
                        .stock(sizeStock.getStock())
                        .build())
                .toList();

        List<Inventory> savedInventories = inventoryRepository.saveAll(inventories);

        for (Inventory inventory : savedInventories) {

            String key = getInventoryKey(
                    inventory.getProductId(),
                    inventory.getSize()
            );

            redisTemplate.opsForValue().set(key, inventory.getStock());
        }
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

    @Transactional
    public void deductInventory(DeductInventoryRequest request) {

        Long remainingStock = deductStockFromRedis(
                request.getProductId(),
                request.getSize(),
                request.getQuantity()
        );

        if (remainingStock == -1L) {
            throw new InventoryNotFoundException("Inventory not found.");
        }

        if (remainingStock == -2L) {
            throw new InsufficientStockException("Insufficient stock.");
        }

        Inventory inventory = inventoryRepository
                .findByProductIdAndSize(
                        request.getProductId(),
                        request.getSize()
                )
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found."));

        inventory.setStock(remainingStock.intValue());

        inventoryRepository.save(inventory);
    }

    
    private Long deductStockFromRedis(Long productId, String size, Integer quantity) {

        String key = getInventoryKey(productId, size);

        return redisTemplate.execute(
                deductStockScript,
                Collections.singletonList(key),
                quantity
        );
    }

    private String getInventoryKey(Long productId, String size) {
        return "inventory:" + productId + ":" + size;
    }
}
