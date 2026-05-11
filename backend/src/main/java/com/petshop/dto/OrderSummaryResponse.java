package com.petshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSummaryResponse {
    private Long id;
    private String orderNumber;
    private String status;
    private LocalDateTime placedAt;
    private BigDecimal totalAmount;
    private int itemCount;

    public OrderSummaryResponse(Long id, String orderNumber, String status,
                                 LocalDateTime placedAt, BigDecimal totalAmount, int itemCount) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.status = status;
        this.placedAt = placedAt;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getStatus() { return status; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public int getItemCount() { return itemCount; }
}
