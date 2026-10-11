package com.ecommerce.inventoryservice.listener;

import com.ecommerce.inventoryservice.event.OrderPlacedEvent;
import com.ecommerce.inventoryservice.service.InventoryService;
import com.ecommerce.notificationservice.event.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventListener {
    private final InventoryService inventoryService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "inventory-queue")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        try {
            boolean allProductsInStock = event.items().stream()
                    .allMatch(item -> inventoryService.isInStock(item.sku(), item.quantity()));

            if (!allProductsInStock) {
                cancelOrder(event, "One or more products are out of stock");
                return;
            }

            event.items().forEach(item -> inventoryService.reduceStock(item.sku(), item.quantity()));
            rabbitTemplate.convertAndSend("order-events", "order.confirmed", event);

            log.info("Stock reduced for order: {}", event.orderNumber());
        } catch (Exception e) {
            log.error("Error occurred while reducing stock for order: {}, Error: {}", event.orderNumber(), e.getMessage());
        }
    }

    private void cancelOrder(OrderPlacedEvent event, String reason) {
        OrderCancelledEvent orderCancelledEvent = new OrderCancelledEvent(event.orderNumber(), event.email(), reason);
        rabbitTemplate.convertAndSend("order-events", "order.cancelled", orderCancelledEvent);
    }
}
