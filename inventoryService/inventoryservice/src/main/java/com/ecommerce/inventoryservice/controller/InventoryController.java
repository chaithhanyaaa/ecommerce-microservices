package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.request.CreateInventoryRequest;
import com.ecommerce.inventoryservice.dto.request.DeductInventoryRequest;
import com.ecommerce.inventoryservice.dto.response.InventoryResponse;
import com.ecommerce.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/internal/inventory")
    @ResponseStatus(HttpStatus.CREATED)
    public void createInventory(@Valid @RequestBody CreateInventoryRequest request)
    {
        System.out.println("hello");

        inventoryService.createInventory(request);
    }

    @GetMapping("/inventory/{productId}")
    public InventoryResponse getInventory(@PathVariable Long productId)
    {


        return inventoryService.getInventory(productId);
    }


    @PostMapping("/internal/inventory/deduct")
    public void deductInventory(
            @Valid @RequestBody DeductInventoryRequest request) {

        inventoryService.deductInventory(request);
    }

}