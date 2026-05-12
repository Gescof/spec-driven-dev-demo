package com.petshop.service;

import com.petshop.dto.RegisterRequest;
import com.petshop.dto.UserProfileResponse;
import com.petshop.exception.DuplicateEmailException;
import com.petshop.model.User;
import com.petshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;

    @InjectMocks AuthService authService;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setName("Test User");
        savedUser.setPasswordHash("$2a$10$hash");
        savedUser.setRegisteredAt(LocalDateTime.now());
    }

    @Test
    void register_newEmail_createsUserAndReturnsProfile() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserProfileResponse result = authService.register(
                new RegisterRequest("test@example.com", "Test User", "password123"));

        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.name()).isEqualTo("Test User");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsDuplicateEmailException() {
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(new RegisterRequest("dup@example.com", "User", "password123")))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getCurrentUser_extractsEmailFromPrincipalAndReturnsProfile() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(savedUser));

        org.springframework.security.core.Authentication auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "test@example.com", null);

        UserProfileResponse result = authService.getCurrentUser(auth);

        assertThat(result.email()).isEqualTo("test@example.com");
    }
}
