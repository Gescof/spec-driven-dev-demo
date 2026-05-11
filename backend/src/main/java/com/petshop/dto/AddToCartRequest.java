package com.petshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public class AddToCartRequest {

    @Positive
    private Long productId;

    @Min(1)
    private int quantity;

    public AddToCartRequest() {}

    public AddToCartRequest(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
