package com.petshop.dto;

import java.math.BigDecimal;

public class ProductDetailResponse extends ProductSummaryResponse {
    private String description;

    public ProductDetailResponse(Long id, String name, BigDecimal price, String imageUrl,
                                  boolean available, CategoryResponse category, String description) {
        super(id, name, price, imageUrl, available, category);
        this.description = description;
    }

    public String getDescription() { return description; }
}
