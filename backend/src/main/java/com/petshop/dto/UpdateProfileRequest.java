package com.petshop.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(min = 1, message = "Name must not be blank")
    private String name;

    private String currentPassword;

    @Size(min = 8)
    private String newPassword;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String name, String currentPassword, String newPassword) {
        this.name = name;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
