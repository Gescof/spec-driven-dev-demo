package com.petshop.service;

import com.petshop.dto.UpdateProfileRequest;
import com.petshop.dto.UserProfileResponse;
import com.petshop.exception.NotFoundException;
import com.petshop.model.User;
import com.petshop.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        return toProfile(user);
    }

    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null ||
                    !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        return toProfile(userRepository.save(user));
    }

    private UserProfileResponse toProfile(User u) {
        return new UserProfileResponse(u.getId(), u.getEmail(), u.getName(), u.getRegisteredAt());
    }
}
