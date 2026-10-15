package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.model.OutboxEvent;

import java.util.List;

public interface OutboxService {
    void saveOrderPlacedEvent(OrderPlacedEvent event, boolean isProcessed);

    List<OutboxEvent> getPendingEvents();

    void markEventAsProcessed(Long id);
}
