package com.ecommerce.orderservice.listener;

import com.ecommerce.notificationservice.event.OrderCancelledEvent;
import com.ecommerce.notificationservice.event.OrderConfirmedEvent;
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
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        orderService.updateOrderStatus(event.orderNumber(), OrderStatus.CONFIRMED);
    }


    @RabbitListener(queues = "order-cancelled-queue")
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        orderService.updateOrderStatus(event.orderNumber(), OrderStatus.CANCELLED);
    }
}
