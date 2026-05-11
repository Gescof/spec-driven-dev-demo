package com.petshop.controller;

import com.petshop.dto.UpdateProfileRequest;
import com.petshop.dto.UserProfileResponse;
import com.petshop.model.User;
import com.petshop.repository.UserRepository;
import com.petshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(
            @Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    private Long resolveUserId(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new com.petshop.exception.NotFoundException("User not found"));
    }
}
