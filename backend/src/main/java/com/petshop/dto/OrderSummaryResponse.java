package com.petshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(Long id, String orderNumber, String status,
                                   LocalDateTime placedAt, BigDecimal totalAmount, int itemCount) {}
