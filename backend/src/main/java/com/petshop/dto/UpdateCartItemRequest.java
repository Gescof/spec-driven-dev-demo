package com.petshop.dto;

import jakarta.validation.constraints.Min;

public class UpdateCartItemRequest {

    @Min(1)
    private int quantity;

    public UpdateCartItemRequest() {}

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
