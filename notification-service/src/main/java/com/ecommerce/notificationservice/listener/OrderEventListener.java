package com.ecommerce.notificationservice.listener;


import com.ecommerce.notificationservice.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {


    @RabbitListener(queues = "notification-queue")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        event.items().forEach(item -> {
            try {
                log.info("Sending email notification to email:{}", event.email());

                log.info("Email sent successfully for order:{}", event.orderNumber());
            } catch (Exception e) {
                log.error("Error occurred while sending email notification: {}", e.getMessage());
            }
        });
    }
}
