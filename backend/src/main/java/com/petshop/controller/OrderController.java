package com.petshop.controller;

import com.petshop.dto.OrderDetailResponse;
import com.petshop.dto.OrderSummaryResponse;
import com.petshop.dto.PlaceOrderRequest;
import com.petshop.model.User;
import com.petshop.repository.UserRepository;
import com.petshop.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getUserOrders(resolveUserId(authentication)));
    }

    @PostMapping
    public ResponseEntity<OrderDetailResponse> placeOrder(
            @RequestBody PlaceOrderRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(resolveUserId(authentication), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrder(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrderDetail(resolveUserId(authentication), id));
    }

    private Long resolveUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new com.petshop.exception.NotFoundException("User not found"));
    }
}
