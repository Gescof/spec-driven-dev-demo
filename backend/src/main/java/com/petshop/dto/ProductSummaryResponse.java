package com.petshop.dto;

import java.math.BigDecimal;

public record ProductSummaryResponse(Long id, String name, BigDecimal price, String imageUrl,
                                     boolean available, CategoryResponse category) {}
