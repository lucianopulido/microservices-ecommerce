package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.model.OutboxEvent;
import com.ecommerce.orderservice.repository.OutboxRepository;
import com.ecommerce.orderservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements OutboxService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void saveOrderPlacedEvent(OrderPlacedEvent event, boolean isProcessed) {
        String payload = objectMapper.writeValueAsString(event);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(event.orderNumber())
                .type("ORDER_PLACED")
                .payload(payload)
                .createdAt(LocalDateTime.now())
                .processed(isProcessed)
                .build();

        this.outboxRepository.save(outboxEvent);
        log.info("Evento asegurado en outbox: {}", event.orderNumber());
    }

    @Override
    public List<OutboxEvent> getPendingEvents() {
        return this.outboxRepository.findByProcessedFalse();
    }

    @Override
    public void markEventAsProcessed(Long id) {
        this.outboxRepository.findById(id).ifPresent(event -> {
            event.setProcessed(true);
            this.outboxRepository.save(event);
            log.info("Evento marcado como procesado: {}", id);
        });
    }
}
