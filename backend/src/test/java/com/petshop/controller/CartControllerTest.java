package com.petshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.dto.AddToCartRequest;
import com.petshop.dto.CartResponse;
import com.petshop.exception.NotFoundException;
import com.petshop.exception.OutOfStockException;
import com.petshop.model.User;
import com.petshop.repository.UserRepository;
import com.petshop.service.CartService;
import com.petshop.service.PetShopUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CartService cartService;
    @MockBean UserRepository userRepository;
    @MockBean PetShopUserDetailsService petShopUserDetailsService;

    @BeforeEach
    void setUp() {
        User u = new User();
        u.setId(1L);
        u.setEmail("test@example.com");
        u.setName("Test User");
        u.setPasswordHash("hash");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(u));
    }

    private CartResponse emptyCart() {
        return new CartResponse(List.of(), BigDecimal.ZERO, 0);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCart_authenticated_returns200() throws Exception {
        when(cartService.getCart(anyLong())).thenReturn(emptyCart());

        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    void getCart_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addToCart_validProduct_returns200() throws Exception {
        when(cartService.addToCart(anyLong(), any())).thenReturn(emptyCart());

        mockMvc.perform(post("/api/v1/cart/items").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AddToCartRequest(1L, 1))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addToCart_productUnavailable_returns400() throws Exception {
        when(cartService.addToCart(anyLong(), any()))
                .thenThrow(new OutOfStockException("Product out of stock"));

        mockMvc.perform(post("/api/v1/cart/items").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AddToCartRequest(1L, 1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addToCart_unknownProduct_returns404() throws Exception {
        when(cartService.addToCart(anyLong(), any()))
                .thenThrow(new NotFoundException("Product not found"));

        mockMvc.perform(post("/api/v1/cart/items").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AddToCartRequest(999L, 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateCartItem_returns200() throws Exception {
        when(cartService.updateQuantity(anyLong(), anyLong(), anyInt())).thenReturn(emptyCart());

        mockMvc.perform(put("/api/v1/cart/items/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":3}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteCartItem_returns200() throws Exception {
        when(cartService.removeItem(anyLong(), anyLong())).thenReturn(emptyCart());

        mockMvc.perform(delete("/api/v1/cart/items/1").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void cartEndpoints_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/cart/items").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
