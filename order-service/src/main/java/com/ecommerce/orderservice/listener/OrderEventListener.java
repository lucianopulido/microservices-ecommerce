package com.ecommerce.orderservice.listener;

import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {
    private final OrderService orderService;

    @RabbitListener(queues = "order-confirmed-queue")
    public void handleOrderConfirmedEvent(OrderPlacedEvent event) {
        orderService.updateOrderStatus(event.orderNumber(), OrderStatus.CONFIRMED);
    }


    @RabbitListener(queues = "order-cancelled-queue")
    public void handleOrderCancelledEvent(OrderPlacedEvent event) {
        orderService.updateOrderStatus(event.orderNumber(), OrderStatus.CANCELLED);
    }
}
