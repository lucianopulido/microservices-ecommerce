package com.ecommerce.orderservice.service;


import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest orderRequest, String userId); // Create

    // List<OrderResponse> getAllOrders();                  // Read All
    List<OrderResponse> getAllByUserId(String userId, boolean isAdmin);

    OrderResponse getOrderById(Long id);                 // Read One

    void deleteOrder(Long id);                           // Delete

    void updateOrderStatus(String orderNumber, OrderStatus status);                 // Update
}