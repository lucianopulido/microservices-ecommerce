package com.ecommerce.orderservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "order-events";
    public static final String QUEUE_CONFIRMED_NAME = "order-confirmed-queue";
    public static final String QUEUE_CANCELLED_NAME = "order-cancelled-queue";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange orderEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue orderConfirmedQueue() {
        return new Queue(QUEUE_CONFIRMED_NAME, true);
    }

    @Bean
    public Binding confirmedBinding(Queue orderConfirmedQueue, TopicExchange orderEventsExchange) {
        return BindingBuilder.bind(orderConfirmedQueue)
                .to(orderEventsExchange)
                .with("order.confirmed");
    }

    @Bean
    public Queue orderCancelledQueue() {
        return new Queue(QUEUE_CANCELLED_NAME, true);
    }

    @Bean
    public Binding cancelledBinding(Queue orderCancelledQueue, TopicExchange orderEventsExchange) {
        return BindingBuilder.bind(orderCancelledQueue)
                .to(orderEventsExchange)
                .with("order.cancelled");
    }
}
