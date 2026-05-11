package com.petshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailResponse extends OrderSummaryResponse {
    private List<OrderLineItemResponse> items;

    public OrderDetailResponse(Long id, String orderNumber, String status, LocalDateTime placedAt,
                                BigDecimal totalAmount, int itemCount, List<OrderLineItemResponse> items) {
        super(id, orderNumber, status, placedAt, totalAmount, itemCount);
        this.items = items;
    }

    public List<OrderLineItemResponse> getItems() { return items; }
}
