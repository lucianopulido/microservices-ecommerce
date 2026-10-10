package com.ecommerce.orderservice.service.impl;


import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.mapper.OrderMapper;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderLineItems;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${order.enabled}")
    private boolean orderEnabled;

    @Override
    @Transactional
    @CircuitBreaker(name = "inventory", fallbackMethod = "handleInventoryServiceFailure")
    @Retry(name = "inventory")
    public OrderResponse placeOrder(OrderRequest orderRequest, String userId) {
        if (!orderEnabled) {
            log.warn("Delivery refused: Order processing is currently disabled.");
            throw new RuntimeException("Order processing is currently disabled.");
        }

        log.info("Colocando nueva orden...");

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsList()
                .stream()
                .map(item -> {
                    String sku = item.getSku();
                    Integer quantity = item.getQuantity();
                    return orderMapper.toOrderLineItems(item);
                }).toList();


        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setOrderLineItemsList(orderLineItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Orden guardada con éxito. ID: {}", savedOrder.getId());


        List<OrderPlacedEvent.OrderItemEvent> orderItems = order.getOrderLineItemsList()
                .stream()
                .map(item -> new OrderPlacedEvent.OrderItemEvent(item.getSku(), item.getPrice().toString(), item.getQuantity()))
                .toList();

        OrderPlacedEvent event = new OrderPlacedEvent(savedOrder.getOrderNumber(), orderRequest.getEmail(), orderItems);
        rabbitTemplate.convertAndSend("order-events", "order.placed", event);
        log.info("Evento enviado a RabbitMQ para la orden: {}", savedOrder.getOrderNumber());
        return orderMapper.toOrderResponse(savedOrder);

    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllByUserId(String userId, boolean isAdmin) {
        List<Order> orders;

        if (isAdmin) {
            orders = orderRepository.findAll();
        } else {
            orders = orderRepository.findByUserId(userId);
        }
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    /*
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }


     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", id));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Orden", "id", id);
        }
        orderRepository.deleteById(id);
        log.info("Orden eliminada. ID: {}", id);
    }

    public OrderResponse handleInventoryServiceFailure(OrderRequest orderRequest, String userId, Throwable throwable) {
        log.error("Circuit Breaker Activado. Fallo en el servicio de inventario. Causa: {}", throwable.getMessage());
        throw new RuntimeException("El servicio de inventario no está disponible. Por favor, inténtelo más tarde.");
    }
}