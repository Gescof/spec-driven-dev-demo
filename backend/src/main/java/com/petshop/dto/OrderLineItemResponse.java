package com.petshop.dto;

import java.math.BigDecimal;

public record OrderLineItemResponse(Long productId, String productName, BigDecimal unitPrice,
                                    int quantity, BigDecimal lineTotal) {}
