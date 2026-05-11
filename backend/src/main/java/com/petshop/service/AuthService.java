package com.petshop.service;

import com.petshop.dto.RegisterRequest;
import com.petshop.dto.UserProfileResponse;
import com.petshop.exception.DuplicateEmailException;
import com.petshop.exception.NotFoundException;
import com.petshop.model.User;
import com.petshop.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already taken: " + request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        return toProfile(saved);
    }

    public UserProfileResponse getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toProfile(user);
    }

    private UserProfileResponse toProfile(User u) {
        return new UserProfileResponse(u.getId(), u.getEmail(), u.getName(), u.getRegisteredAt());
    }
}
