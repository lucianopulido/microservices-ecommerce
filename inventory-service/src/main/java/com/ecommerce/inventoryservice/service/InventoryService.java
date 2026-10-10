package com.ecommerce.inventoryservice.service;


import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;

import java.util.List;

public interface InventoryService {
    boolean isInStock(String sku, Integer quantity);

    InventoryResponse createInventory(InventoryRequest inventoryRequest);

    List<InventoryResponse> getAllInventory();

    InventoryResponse updateInventory(Long id, InventoryRequest inventoryRequest);

    void deleteInventory(Long id);

    void reduceStock(String sku, Integer quantity);
}