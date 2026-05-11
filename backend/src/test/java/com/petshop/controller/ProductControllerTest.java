package com.petshop.controller;

import com.petshop.dto.CategoryResponse;
import com.petshop.dto.ProductDetailResponse;
import com.petshop.dto.ProductPageResponse;
import com.petshop.dto.ProductSummaryResponse;
import com.petshop.exception.NotFoundException;
import com.petshop.service.PetShopUserDetailsService;
import com.petshop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean ProductService productService;
    @MockBean PetShopUserDetailsService petShopUserDetailsService;

    private CategoryResponse cat() { return new CategoryResponse(1L, "Dogs", "Canine"); }

    private ProductSummaryResponse summary(Long id) {
        return new ProductSummaryResponse(id, "Product " + id, BigDecimal.valueOf(9.99), "http://img", true, cat());
    }

    private ProductPageResponse page(List<ProductSummaryResponse> items) {
        return new ProductPageResponse(items, items.size(), 1, 0, 20);
    }

    @Test
    @WithMockUser
    void listProducts_noParams_returns200Page() throws Exception {
        when(productService.listProducts(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page(List.of(summary(1L), summary(2L))));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser
    void listProducts_withCategoryId_returns200FilteredPage() throws Exception {
        when(productService.listProducts(eq(1L), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page(List.of(summary(1L))));

        mockMvc.perform(get("/api/v1/products?categoryId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void listProducts_withSearch_returns200Page() throws Exception {
        when(productService.listProducts(isNull(), eq("cat"), isNull(), any(Pageable.class)))
                .thenReturn(page(List.of(summary(3L))));

        mockMvc.perform(get("/api/v1/products?search=cat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3));
    }

    @Test
    @WithMockUser
    void getProductById_found_returns200Detail() throws Exception {
        ProductDetailResponse detail = new ProductDetailResponse(
                1L, "Dog Food", BigDecimal.valueOf(29.99), "http://img", true, cat(), "Great food");
        when(productService.getById(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dog Food"));
    }

    @Test
    @WithMockUser
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getById(999L)).thenThrow(new NotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getFeatured_returns200Array() throws Exception {
        when(productService.getFeatured()).thenReturn(List.of(summary(1L), summary(2L)));

        mockMvc.perform(get("/api/v1/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
