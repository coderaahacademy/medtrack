package com.medtrack.user.service;

import com.medtrack.user.api.LoginRequest;
import com.medtrack.user.api.LoginResponse;
import com.medtrack.user.api.RegisterUserRequest;
import com.medtrack.user.api.UserResponse;
import com.medtrack.entity.User;
import com.medtrack.entity.UserRole;
import com.medtrack.enums.UserStatus;
import com.medtrack.enums.Role;
import com.medtrack.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(hashPassword(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(request.getRole());
        user.getRoles().add(userRole);

        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return new LoginResponse(false, "Invalid email or password", null);
        }
        User user = optionalUser.get();
        String expectedHash = hashPassword(request.getPassword());
        if (!expectedHash.equals(user.getPasswordHash())) {
            return new LoginResponse(false, "Invalid email or password", null);
        }
        return new LoginResponse(true, "Login successful", toUserResponse(user));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private String hashPassword(String password) {
        return "hashed_" + password;
    }

    private UserResponse toUserResponse(User user) {
        Role mainRole = user.getRoles().isEmpty() ? null : user.getRoles().get(0).getRole();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                mainRole,
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
