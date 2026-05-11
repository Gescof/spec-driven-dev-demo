package com.petshop.service;

import com.petshop.dto.AddToCartRequest;
import com.petshop.dto.CartResponse;
import com.petshop.exception.OutOfStockException;
import com.petshop.model.*;
import com.petshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartAndOrderServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock ProductRepository productRepository;

    @InjectMocks CartService cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test");
        user.setPasswordHash("hash");

        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Dogs");

        product = new Product();
        product.setId(10L);
        product.setName("Dog Food");
        product.setPrice(BigDecimal.valueOf(29.99));
        product.setCategory(cat);
        product.setAvailable(true);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
    }

    @Test
    void addToCart_newProduct_createsCartItem() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenReturn(cart);

        CartResponse result = cartService.addToCart(1L, new AddToCartRequest(10L, 1));

        verify(cartItemRepository).save(any(CartItem.class));
        assertThat(result).isNotNull();
    }

    @Test
    void addToCart_existingProduct_incrementsQuantity() {
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProduct(product);
        existing.setQuantity(2);
        cart.getItems().add(existing);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any())).thenReturn(cart);

        cartService.addToCart(1L, new AddToCartRequest(10L, 1));

        assertThat(existing.getQuantity()).isEqualTo(3);
    }

    @Test
    void addToCart_unavailableProduct_throwsOutOfStockException() {
        product.setAvailable(false);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addToCart(1L, new AddToCartRequest(10L, 1)))
                .isInstanceOf(OutOfStockException.class);
    }

    @Test
    void updateQuantity_changesCartItemQuantity() {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(item));
        when(cartRepository.save(any())).thenReturn(cart);

        cartService.updateQuantity(1L, 10L, 5);

        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    void removeItem_deletesCartItem() {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(1);
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(item));
        when(cartRepository.save(any())).thenReturn(cart);

        cartService.removeItem(1L, 10L);

        verify(cartItemRepository).delete(item);
    }
}
