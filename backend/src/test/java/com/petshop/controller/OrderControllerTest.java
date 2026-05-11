package com.petshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.dto.OrderDetailResponse;
import com.petshop.dto.OrderSummaryResponse;
import com.petshop.dto.PlaceOrderRequest;
import com.petshop.exception.NotFoundException;
import com.petshop.model.User;
import com.petshop.repository.UserRepository;
import com.petshop.service.OrderService;
import com.petshop.service.PetShopUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean OrderService orderService;
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

    private OrderDetailResponse detail() {
        return new OrderDetailResponse(1L, "ORD-abc", "CONFIRMED",
                LocalDateTime.now(), BigDecimal.valueOf(29.99), 1, List.of());
    }

    private OrderSummaryResponse summary() {
        return new OrderSummaryResponse(1L, "ORD-abc", "CONFIRMED",
                LocalDateTime.now(), BigDecimal.valueOf(29.99), 1);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrders_authenticated_returns200() throws Exception {
        when(orderService.getUserOrders(anyLong())).thenReturn(List.of(summary()));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOrders_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void placeOrder_withItems_returns201() throws Exception {
        when(orderService.placeOrder(anyLong(), any())).thenReturn(detail());

        mockMvc.perform(post("/api/v1/orders").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PlaceOrderRequest("John", "1234"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-abc"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void placeOrder_emptyCart_returns400() throws Exception {
        when(orderService.placeOrder(anyLong(), any()))
                .thenThrow(new IllegalStateException("Cart is empty"));

        mockMvc.perform(post("/api/v1/orders").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PlaceOrderRequest("John", "1234"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrder_owned_returns200() throws Exception {
        when(orderService.getOrderDetail(anyLong(), anyLong())).thenReturn(detail());

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrder_notOwner_returns403() throws Exception {
        when(orderService.getOrderDetail(anyLong(), anyLong()))
                .thenThrow(new AccessDeniedException("Not your order"));

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrder_notFound_returns404() throws Exception {
        when(orderService.getOrderDetail(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Order not found"));

        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound());
    }
}
