package com.petshop.dto;

import java.math.BigDecimal;

public record CartItemResponse(Long productId, String productName, String productImageUrl,
                               BigDecimal unitPrice, int quantity, BigDecimal lineTotal,
                               boolean available) {}
