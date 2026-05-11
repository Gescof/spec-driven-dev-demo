package com.petshop.dto;

import java.math.BigDecimal;

public class CartItemResponse {
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;
    private boolean available;

    public CartItemResponse(Long productId, String productName, String productImageUrl,
                             BigDecimal unitPrice, int quantity, BigDecimal lineTotal, boolean available) {
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.available = available;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductImageUrl() { return productImageUrl; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public boolean isAvailable() { return available; }
}
