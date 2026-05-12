package com.petshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(Long id, String orderNumber, String status,
                                  LocalDateTime placedAt, BigDecimal totalAmount, int itemCount,
                                  List<OrderLineItemResponse> items) {}
