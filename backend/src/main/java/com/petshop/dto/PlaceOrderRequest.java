package com.petshop.dto;

import jakarta.validation.constraints.Pattern;

public class PlaceOrderRequest {
    private String cardholderName;

    @Pattern(regexp = "\\d{4}", message = "Must be exactly 4 digits")
    private String cardNumberLast4;

    public PlaceOrderRequest() {}

    public PlaceOrderRequest(String cardholderName, String cardNumberLast4) {
        this.cardholderName = cardholderName;
        this.cardNumberLast4 = cardNumberLast4;
    }

    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }
    public String getCardNumberLast4() { return cardNumberLast4; }
    public void setCardNumberLast4(String cardNumberLast4) { this.cardNumberLast4 = cardNumberLast4; }
}
