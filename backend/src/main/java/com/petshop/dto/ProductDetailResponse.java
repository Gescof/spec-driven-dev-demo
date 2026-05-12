package com.petshop.dto;

import java.math.BigDecimal;

public record ProductDetailResponse(Long id, String name, BigDecimal price, String imageUrl,
                                    boolean available, CategoryResponse category, String description) {}
