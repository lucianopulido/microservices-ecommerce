package com.ecommerce.notificationservice.event;

import lombok.Builder;

@Builder
public record OrderConfirmedEvent(String orderNumber, String email) {
}
