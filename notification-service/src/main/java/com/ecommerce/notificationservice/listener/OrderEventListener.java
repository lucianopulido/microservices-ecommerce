package com.ecommerce.notificationservice.listener;


import com.ecommerce.notificationservice.event.OrderCancelledEvent;
import com.ecommerce.notificationservice.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = "notification-queue")
public class OrderEventListener {

    private final JavaMailSender mailSender;

    @RabbitHandler
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent for order:{}", event.orderNumber());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("pedidos@ecommerce.com");
        message.setTo(event.email());
        message.setSubject("Order Confirmada - " + event.orderNumber());
        message.setText("Hola!\n"
                + "Tu pedido con numero: " + event.orderNumber() + " ha sido recibido exitosamente.\n" +
                "Gracias por comprar con nosotros!");
        mailSender.send(message);
        log.info("Email sent successfully for order:{} to email:{}", event.orderNumber(), event.email());

    }

    @RabbitHandler
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent for order:{}", event.orderNumber());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("pedidos@ecommerce.com");
        message.setTo(event.email());
        message.setSubject("Order Cancelada - " + event.orderNumber());
        message.setText("Hola!\n"
                + "Tu pedido con numero: " + event.orderNumber() + " ha sido cancelado.\n" +
                "Lo sentimos por las molestias.");
        mailSender.send(message);

        log.info("Email sent successfully for order:{} to email:{}", event.orderNumber(), event.email());

    }
}
