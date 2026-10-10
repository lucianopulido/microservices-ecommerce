package com.ecommerce.inventoryservice.listener;

import com.ecommerce.inventoryservice.event.OrderPlacedEvent;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventListener {
    private final InventoryService inventoryService;

    @RabbitListener(queues = "inventory-queue")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        event.items().forEach(item -> {
            try {
                inventoryService.reduceStock(item.sku(), item.quantity());
                log.info("Stock reduced for SKU: {}, Quantity: {}", item.sku(), item.quantity());
            } catch (Exception e) {
                log.error("Error occurred while reducing stock for SKU: {}, Error: {}", item.sku(), e.getMessage());
            }
        });
    }
}
