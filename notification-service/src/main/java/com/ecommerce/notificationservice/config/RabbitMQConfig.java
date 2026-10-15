package com.ecommerce.notificationservice.config;

import com.ecommerce.notificationservice.event.OrderCancelledEvent;
import com.ecommerce.notificationservice.event.OrderConfirmedEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "order-events";
    public static final String QUEUE_NAME = "notification-queue";
    public static final String DEAD_LETTER_EXCHANGE_NAME = "notification-dlx";
    public static final String DEAD_LETTER_QUEUE_NAME = "notification-dlq";
    public static final String DEAD_LETTER_HEADER_EXCHANGE = "x-dead-letter-exchange";
    public static final String DEAD_LETTER_HEADER_ROUTING_KEY = "x-dead-letter-routing-key";
    public static final String DEAD_LETTER_ROUTING_KEY = "notification.dead";
    public static final String INVENTORY_ORDER_PLACED_EVENT_TYPE = "com.ecommerce.inventoryservice.event.OrderConfirmedEvent";
    public static final String INVENTORY_ORDER_CANCELLED_EVENT_TYPE = "com.ecommerce.inventoryservice.event.OrderCancelledEvent";


    @Bean
    public MessageConverter messageConverter() {
        // Usamos la versión moderna de Jackson para Spring Boot 4
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();

        // Permitimos que confíe en nuestros paquetes de eventos
        classMapper.setTrustedPackages("*");

        // MAPEAREMOS LAS IDENTIDADES:
        // "Nombre de clase que viene del emisor" -> "Clase local que la recibe"
        Map<String, Class<?>> idClassMapping = new HashMap<>();

        // Si viene un 'OrderPlacedEvent' desde Inventario, lo tratamos como 'OrderConfirmedEvent' local
        idClassMapping.put(INVENTORY_ORDER_PLACED_EVENT_TYPE, OrderConfirmedEvent.class);

        // Si viene una cancelación, la mapeamos a nuestra clase local de cancelación
        idClassMapping.put(INVENTORY_ORDER_CANCELLED_EVENT_TYPE, OrderCancelledEvent.class);

        classMapper.setIdClassMapping(idClassMapping);
        converter.setClassMapper(classMapper);

        return converter;
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument(DEAD_LETTER_HEADER_EXCHANGE, DEAD_LETTER_EXCHANGE_NAME)
                .withArgument(DEAD_LETTER_HEADER_ROUTING_KEY, DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange orderEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE_NAME);
    }

    @Bean
    public Binding confirmedBinding(Queue notificationQueue, TopicExchange orderEventsExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(orderEventsExchange)
                .with("order.confirmed");
    }

    @Bean
    public Binding cancelledBinding(Queue notificationQueue, TopicExchange orderEventsExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(orderEventsExchange)
                .with("order.cancelled");
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with("notification.dead");
    }
}
