package com.petshop.controller;

import com.petshop.dto.AddToCartRequest;
import com.petshop.dto.CartResponse;
import com.petshop.dto.UpdateCartItemRequest;
import com.petshop.model.User;
import com.petshop.repository.UserRepository;
import com.petshop.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(resolveUserId(authentication)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody AddToCartRequest request, Authentication authentication) {
        return ResponseEntity.ok(cartService.addToCart(resolveUserId(authentication), request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                cartService.updateQuantity(resolveUserId(authentication), productId, request.getQuantity()));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long productId, Authentication authentication) {
        return ResponseEntity.ok(cartService.removeItem(resolveUserId(authentication), productId));
    }

    private Long resolveUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new com.petshop.exception.NotFoundException("User not found"));
    }
}
