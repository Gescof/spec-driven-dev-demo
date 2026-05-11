package com.petshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.dto.UpdateProfileRequest;
import com.petshop.dto.UserProfileResponse;
import com.petshop.model.User;
import com.petshop.repository.UserRepository;
import com.petshop.service.PetShopUserDetailsService;
import com.petshop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;
    @MockBean UserRepository userRepository;
    @MockBean PetShopUserDetailsService petShopUserDetailsService;

    @BeforeEach
    void setUp() {
        User u = new User();
        u.setId(1L);
        u.setEmail("test@example.com");
        u.setName("Test User");
        u.setPasswordHash("hash");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(u));
    }

    private UserProfileResponse profile() {
        return new UserProfileResponse(1L, "test@example.com", "Test User", LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getMe_authenticated_returns200() throws Exception {
        when(userService.getProfile(anyLong())).thenReturn(profile());

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getMe_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateMe_validName_returns200() throws Exception {
        UserProfileResponse updated = new UserProfileResponse(1L, "test@example.com", "New Name", LocalDateTime.now());
        when(userService.updateProfile(anyLong(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/users/me").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateProfileRequest("New Name", null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateMe_blankName_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/users/me").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateProfileRequest("", null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }
}
