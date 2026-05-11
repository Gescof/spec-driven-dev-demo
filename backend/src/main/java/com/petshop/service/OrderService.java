package com.petshop.service;

import com.petshop.dto.OrderDetailResponse;
import com.petshop.dto.OrderLineItemResponse;
import com.petshop.dto.OrderSummaryResponse;
import com.petshop.dto.PlaceOrderRequest;
import com.petshop.exception.NotFoundException;
import com.petshop.exception.OutOfStockException;
import com.petshop.model.*;
import com.petshop.repository.CartRepository;
import com.petshop.repository.OrderRepository;
import com.petshop.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final CartService cartService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(CartService cartService, CartRepository cartRepository,
                        OrderRepository orderRepository, UserRepository userRepository) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public OrderDetailResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        List<String> unavailable = cart.getItems().stream()
                .filter(i -> !i.getProduct().isAvailable())
                .map(i -> i.getProduct().getName())
                .toList();
        if (!unavailable.isEmpty()) {
            throw new OutOfStockException("Out of stock: " + String.join(", ", unavailable));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID());
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(cartItem.getProduct());
            oi.setProductName(cartItem.getProduct().getName());
            oi.setUnitPrice(cartItem.getProduct().getPrice());
            oi.setQuantity(cartItem.getQuantity());
            return oi;
        }).collect(Collectors.toList());

        order.setItems(orderItems);
        BigDecimal total = orderItems.stream()
                .map(oi -> oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);

        return toDetail(saved);
    }

    public List<OrderSummaryResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByPlacedAtDesc(userId)
                .stream().map(this::toSummary).toList();
    }

    public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (!order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Order does not belong to you");
        }
        return toDetail(order);
    }

    private OrderSummaryResponse toSummary(Order o) {
        return new OrderSummaryResponse(o.getId(), o.getOrderNumber(), o.getStatus().name(),
                o.getPlacedAt(), o.getTotalAmount(), o.getItems().size());
    }

    private OrderDetailResponse toDetail(Order o) {
        List<OrderLineItemResponse> lines = o.getItems().stream().map(oi -> {
            BigDecimal lineTotal = oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity()));
            return new OrderLineItemResponse(oi.getProduct().getId(), oi.getProductName(),
                    oi.getUnitPrice(), oi.getQuantity(), lineTotal);
        }).toList();
        return new OrderDetailResponse(o.getId(), o.getOrderNumber(), o.getStatus().name(),
                o.getPlacedAt(), o.getTotalAmount(), o.getItems().size(), lines);
    }
}
