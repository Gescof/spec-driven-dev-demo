package com.petshop.dto;

import java.time.LocalDateTime;

public class UserProfileResponse {
    private Long id;
    private String email;
    private String name;
    private LocalDateTime registeredAt;

    public UserProfileResponse(Long id, String email, String name, LocalDateTime registeredAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.registeredAt = registeredAt;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
}
