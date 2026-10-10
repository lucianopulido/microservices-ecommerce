package com.ecommerce.notificationservice.listener;


import com.ecommerce.notificationservice.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final JavaMailSender mailSender;


    @RabbitListener(queues = "notification-queue")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Procesando evento para orden {}. Usuario SMTP: {}", event.orderNumber(), env.getProperty("spring.mail.username"));
        try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("pedidos@ecommerce.com");
                message.setTo(event.email());
                message.setSubject("Order Confirmada - " + event.orderNumber());
                message.setText("Hola!\n"
                        + "Tu pedido con numero: " + event.orderNumber() + "ha sido recibido exitosamente.\n" +
                        "Gracias por comprar con nosotros!");
                mailSender.send(message);
                log.info("Sending email notification to email:{}", event.email());

                log.info("Email sent successfully for order:{}", event.orderNumber());
            } catch (Exception e) {
                log.error("Error occurred while sending email notification: {}", e.getMessage());
            }
    }
}
