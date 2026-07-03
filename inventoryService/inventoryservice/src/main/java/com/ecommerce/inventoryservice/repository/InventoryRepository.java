package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByProductId(Long productId);
    Optional<Inventory> findByProductIdAndSize(Long productId, String size);

}