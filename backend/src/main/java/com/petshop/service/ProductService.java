package com.petshop.service;

import com.petshop.dto.CategoryResponse;
import com.petshop.dto.ProductDetailResponse;
import com.petshop.dto.ProductPageResponse;
import com.petshop.dto.ProductSummaryResponse;
import com.petshop.exception.NotFoundException;
import com.petshop.model.Product;
import com.petshop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductPageResponse listProducts(Long categoryId, String search, Boolean available, Pageable pageable) {
        String searchPattern = search != null ? "%" + search.toLowerCase() + "%" : null;
        Page<Product> page = productRepository.findByFilters(categoryId, search, searchPattern, available, pageable);
        List<ProductSummaryResponse> items = page.getContent().stream().map(this::toSummary).toList();
        return new ProductPageResponse(items, page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize());
    }

    public ProductDetailResponse getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        return toDetail(p);
    }

    public List<ProductSummaryResponse> getFeatured() {
        return productRepository.findTop8ByAvailableTrueOrderByCreatedAtDesc()
                .stream().map(this::toSummary).toList();
    }

    private CategoryResponse toCategory(com.petshop.model.Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
    }

    private ProductSummaryResponse toSummary(Product p) {
        return new ProductSummaryResponse(p.getId(), p.getName(), p.getPrice(),
                p.getImageUrl(), p.isAvailable(), toCategory(p.getCategory()));
    }

    private ProductDetailResponse toDetail(Product p) {
        return new ProductDetailResponse(p.getId(), p.getName(), p.getPrice(),
                p.getImageUrl(), p.isAvailable(), toCategory(p.getCategory()), p.getDescription());
    }
}
