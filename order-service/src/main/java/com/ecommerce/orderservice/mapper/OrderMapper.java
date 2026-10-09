package com.ecommerce.orderservice.mapper;


import com.ecommerce.orderservice.dto.OrderLineItemsRequest;
import com.ecommerce.orderservice.dto.OrderLineItemsResponse;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderLineItems;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // 1. De Request a Entidad
    // Mapeamos explícitamente la lista porque los nombres no coinciden
    Order toOrder(OrderRequest orderRequest);

    // Método auxiliar (MapStruct lo usa automáticamente para convertir cada ítem de la lista)
    // Aquí NO hace falta @Mapping porque los campos (sku, price, quantity) se llaman igual.
    OrderLineItems toOrderLineItems(OrderLineItemsRequest orderLineItemsRequest);


    // 2. De Entidad a Response
    // Mapeamos explícitamente la lista de vuelta
    OrderResponse toOrderResponse(Order order);

    // Método auxiliar para la respuesta
    // Aquí NO hace falta @Mapping porque los campos (id, sku, price, quantity) se llaman igual.
    OrderLineItemsResponse toOrderLineItemsResponse(OrderLineItems orderLineItems);
}