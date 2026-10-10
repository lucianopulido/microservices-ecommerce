package com.ecommerce.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    @NotEmpty(message = "La orden debe contener al menos un item")
    @Valid
    private List<OrderLineItemsRequest> orderLineItemsList;

    @NotBlank
    @Email(message = "El formato del correo electrónico no es válido")
    private String email;
}