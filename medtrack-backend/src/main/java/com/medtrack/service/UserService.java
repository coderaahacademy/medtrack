package com.medtrack.service;

import com.medtrack.dto.LoginRequest;
import com.medtrack.dto.LoginResponse;
import com.medtrack.dto.RegisterUserRequest;
import com.medtrack.dto.UserResponse;
import com.medtrack.entity.User;
import com.medtrack.entity.UserRole;
import com.medtrack.enums.Role;
import com.medtrack.enums.UserStatus;
import com.medtrack.repository.UserRepository;
import com.medtrack.security.JwtService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Public registration with ADMIN role is not allowed");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        // Securely hash raw password using BCrypt password encoder
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
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
        // Authenticate user credentials via Spring Security AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String token = jwtService.generateToken(user);
        long expiresInSeconds = jwtService.getExpirationMs() / 1000;

        return new LoginResponse(
                true,
                "Login successful",
                token,
                "Bearer",
                expiresInSeconds,
                toUserResponse(user)
        );
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
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
