package com.petshop.dto;

import java.math.BigDecimal;

public class ProductSummaryResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private boolean available;
    private CategoryResponse category;

    public ProductSummaryResponse(Long id, String name, BigDecimal price, String imageUrl,
                                   boolean available, CategoryResponse category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.available = available;
        this.category = category;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public boolean isAvailable() { return available; }
    public CategoryResponse getCategory() { return category; }
}
