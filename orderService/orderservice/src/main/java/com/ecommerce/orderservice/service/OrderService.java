package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.InventoryClient;
import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.request.DeductInventoryRequest;
import com.ecommerce.orderservice.dto.request.OrderRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.dto.response.ProductResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderItem;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.repository.OrderItemRepository;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {

        Long userId = getLoggedInUserId();

        ProductResponse product =
                productClient.getProduct(request.getProductId());

        if (!product.getIsActive()) {
            throw new RuntimeException("Product is inactive.");
        }

        inventoryClient.deductInventory(
                DeductInventoryRequest.builder()
                        .productId(request.getProductId())
                        .size(request.getSize())
                        .quantity(request.getQuantity())
                        .build()
        );

        BigDecimal totalAmount = product.getOfferPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = new Order();

        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        OrderItem orderItem = new OrderItem();

        orderItem.setOrder(savedOrder);
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setPriceAtPurchase(product.getOfferPrice());
        orderItem.setSize(request.getSize());
        orderItem.setQuantity(request.getQuantity());

        orderItemRepository.save(orderItem);

        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .status(savedOrder.getStatus())
                .totalAmount(savedOrder.getTotalAmount())
                .build();
    }

    private Long getLoggedInUserId() {

        CustomUserPrincipal principal =
                (CustomUserPrincipal) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        return principal.getUserId();
    }
}