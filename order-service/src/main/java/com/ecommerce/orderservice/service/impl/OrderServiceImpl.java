package com.ecommerce.orderservice.service.impl;


import com.ecommerce.orderservice.client.InventoryClient;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.mapper.OrderMapper;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderLineItems;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient;
    //    private final WebClient webClientBuilder;
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

            // Mapeo manual de items para asegurar la lista
            List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsList()
                    .stream()
                    .map(item -> {
                        String sku = item.getSku();
                        Integer quantity = item.getQuantity();

                        try {
                            this.inventoryClient.reduceStock(sku, quantity);
                            return orderMapper.toOrderLineItems(item);
//                        webClientBuilder.build().put()
//                                .uri("http://localhost:8082/api/v1/inventory/reduce/" + sku,
//                                        uriBuilder -> uriBuilder.queryParam("quantity", quantity).build())
//                                .retrieve()
//                                .bodyToMono(String.class)
//                                .block();
                        } catch (Exception e) {
                            log.error("Error al reducir el stock del producto: {}", sku, e);
                            throw new IllegalArgumentException("No se pudo procesar la orden: stock insuficiente o error de inventario");
                        }


                    })
                    .toList();

            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setUserId(userId);
            order.setOrderLineItemsList(orderLineItems);

            // Guardamos y capturamos la entidad persistida
            Order savedOrder = orderRepository.save(order);
            log.info("Orden guardada con éxito. ID: {}", savedOrder.getId());
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