package com.petshop.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(Long id, String email, String name, LocalDateTime registeredAt) {}
