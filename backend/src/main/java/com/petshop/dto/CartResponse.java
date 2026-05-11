package com.petshop.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    private List<CartItemResponse> items;
    private BigDecimal grandTotal;
    private int itemCount;

    public CartResponse(List<CartItemResponse> items, BigDecimal grandTotal, int itemCount) {
        this.items = items;
        this.grandTotal = grandTotal;
        this.itemCount = itemCount;
    }

    public List<CartItemResponse> getItems() { return items; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public int getItemCount() { return itemCount; }
}
