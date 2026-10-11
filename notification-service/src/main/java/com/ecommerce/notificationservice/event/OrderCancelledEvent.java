package com.ecommerce.notificationservice.event;

public record OrderCancelledEvent(String orderNumber, String email, String reason) {
}
