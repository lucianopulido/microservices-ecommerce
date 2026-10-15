package com.ecommerce.orderservice.scheduler;

import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.model.OutboxEvent;
import com.ecommerce.orderservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageRelayer {
    private final RabbitTemplate rabbitTemplate;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 10000)
    public void relayMessage() {

        List<OutboxEvent> pendingEvents = this.outboxService.getPendingEvents();

        if (!pendingEvents.isEmpty()) {
            log.info("Se encontraron {} eventos pendientes en la tabla de event outbox", pendingEvents.size());

            for (OutboxEvent event : pendingEvents) {
                try {
                    OrderPlacedEvent orderPlacedEvent = this.objectMapper.readValue(event.getPayload(), OrderPlacedEvent.class);
                    this.rabbitTemplate.convertAndSend("order-events", "order.placed", orderPlacedEvent);
                    this.outboxService.markEventAsProcessed(event.getId());
                    log.info("Evento enviado a RabbitMQ y marcado como procesado: {}", event.getId());
                } catch (AmqpException e) {
                    log.error("Error al procesar el evento: {} error: {}", event.getId(), e.getMessage());
                }
            }
        }

    }
}
