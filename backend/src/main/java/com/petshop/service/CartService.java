package com.petshop.service;

import com.petshop.dto.AddToCartRequest;
import com.petshop.dto.CartItemResponse;
import com.petshop.dto.CartResponse;
import com.petshop.exception.NotFoundException;
import com.petshop.exception.OutOfStockException;
import com.petshop.model.Cart;
import com.petshop.model.CartItem;
import com.petshop.model.Product;
import com.petshop.model.User;
import com.petshop.repository.CartItemRepository;
import com.petshop.repository.CartRepository;
import com.petshop.repository.ProductRepository;
import com.petshop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found: " + userId));
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cart);
    }

    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + request.productId()));
        if (!product.isAvailable()) {
            throw new OutOfStockException("Product is out of stock: " + product.getName());
        }
        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .ifPresentOrElse(
                        item -> {
                            item.setQuantity(item.getQuantity() + request.quantity());
                            cartItemRepository.save(item);
                        },
                        () -> {
                            CartItem item = new CartItem();
                            item.setCart(cart);
                            item.setProduct(product);
                            item.setQuantity(request.quantity());
                            cartItemRepository.save(item);
                        });
        return toResponse(cartRepository.save(cart));
    }

    public CartResponse updateQuantity(Long userId, Long productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return toResponse(cartRepository.save(cart));
    }

    public CartResponse removeItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toResponse(cartRepository.save(cart));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(i -> {
            BigDecimal lineTotal = i.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(i.getQuantity()));
            return new CartItemResponse(
                    i.getProduct().getId(), i.getProduct().getName(),
                    i.getProduct().getImageUrl(), i.getProduct().getPrice(),
                    i.getQuantity(), lineTotal, i.getProduct().isAvailable());
        }).toList();
        BigDecimal grandTotal = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, grandTotal, items.size());
    }
}
