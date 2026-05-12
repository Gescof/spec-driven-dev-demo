package com.petshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record AddToCartRequest(@Positive Long productId, @Min(1) int quantity) {}
