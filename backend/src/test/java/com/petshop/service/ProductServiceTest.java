package com.petshop.service;

import com.petshop.dto.ProductDetailResponse;
import com.petshop.dto.ProductPageResponse;
import com.petshop.dto.ProductSummaryResponse;
import com.petshop.exception.NotFoundException;
import com.petshop.model.Category;
import com.petshop.model.Product;
import com.petshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductService productService;

    private Category cat;
    private Product product;

    @BeforeEach
    void setUp() {
        cat = new Category();
        cat.setId(1L);
        cat.setName("Dogs");
        cat.setDescription("Canine");

        product = new Product();
        product.setId(1L);
        product.setName("Dog Food");
        product.setDescription("Tasty food");
        product.setPrice(BigDecimal.valueOf(29.99));
        product.setCategory(cat);
        product.setImageUrl("http://img");
        product.setAvailable(true);
    }

    @Test
    void listProducts_noFilter_returnsFullPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findByFilters(isNull(), isNull(), isNull(), eq(pageable))).thenReturn(page);

        ProductPageResponse result = productService.listProducts(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listProducts_withCategoryId_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findByFilters(eq(1L), isNull(), isNull(), eq(pageable))).thenReturn(page);

        ProductPageResponse result = productService.listProducts(1L, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void listProducts_withSearch_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findByFilters(isNull(), eq("dog"), isNull(), eq(pageable))).thenReturn(page);

        ProductPageResponse result = productService.listProducts(null, "dog", null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getById_found_returnsProductDetail() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailResponse result = productService.getById(1L);

        assertThat(result.getName()).isEqualTo("Dog Food");
        assertThat(result.getDescription()).isEqualTo("Tasty food");
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getFeatured_returnsAtMost8Items() {
        when(productRepository.findTop8ByAvailableTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(product));

        List<ProductSummaryResponse> result = productService.getFeatured();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isAvailable()).isTrue();
    }
}
