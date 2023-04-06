package com.viajero.orderservice.service;

import com.viajero.orderservice.dto.InventoryResponse;
import com.viajero.orderservice.dto.OrderLineItemsDto;
import com.viajero.orderservice.dto.OrderRequest;
import com.viajero.orderservice.model.Order;
import com.viajero.orderservice.model.OrderLineItems;
import com.viajero.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;

    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems =
                orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToDto).toList();
        order.setOrderLineItemsList(orderLineItems);

        List<String> codes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

        // synchronous request
        InventoryResponse[] res = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", codes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        assert res != null;
        boolean check = Arrays.stream(res).allMatch(InventoryResponse::isInStock);

        if (check) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is not in stock.");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
