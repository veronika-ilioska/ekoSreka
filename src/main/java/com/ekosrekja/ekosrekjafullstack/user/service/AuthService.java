package com.ekosrekja.ekosrekjafullstack.user.service;

import com.ekosrekja.ekosrekjafullstack.user.dto.LoginRequest;
import com.ekosrekja.ekosrekjafullstack.user.dto.SignupRequest;
import com.ekosrekja.ekosrekjafullstack.user.dto.UserResponse;
import com.ekosrekja.ekosrekjafullstack.user.entity.User;
import com.ekosrekja.ekosrekjafullstack.user.repository.UserRepository;
import com.ekosrekja.ekosrekjafullstack.admin.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private AdminRepository adminRepository;

    public UserResponse signup(SignupRequest signupRequest) {
        // Check if user already exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check if passwords match
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Create new user
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());

        User savedUser = userRepository.save(user);

        return mapUserToResponse(savedUser);
    }

    public UserResponse login(LoginRequest loginRequest) {
        User user =
                userRepository
                        .findByUsername(loginRequest.getUsername())
                        .orElseThrow(
                                () -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return mapUserToResponse(user);
    }

    public UserResponse getUserById(Long id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapUserToResponse(user);
    }

    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setCreatedAt(user.getCreatedAt());
        response.setIsAdmin(Boolean.TRUE.equals(user.getIsAdmin()) || adminRepository.existsByUserId(user.getId()));
        return response;
    }
}


