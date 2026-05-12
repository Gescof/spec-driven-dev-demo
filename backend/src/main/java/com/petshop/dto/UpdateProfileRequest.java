package com.petshop.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 1) String name,
        String currentPassword,
        @Size(min = 8) String newPassword) {}
