package com.petshop.dto;

import jakarta.validation.constraints.Pattern;

public record PlaceOrderRequest(String cardholderName, @Pattern(regexp = "\\d{4}") String cardNumberLast4) {}
