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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private RegisterUserRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterUserRequest();
        registerRequest.setEmail("doctor@example.com");
        registerRequest.setPassword("SecurePass123!");
        registerRequest.setRole(Role.DOCTOR);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("doctor@example.com");
        loginRequest.setPassword("SecurePass123!");
    }

    @Test
    void shouldRegisterUserWithBCryptHashAndActiveStatus() {
        when(userRepository.existsByEmail("doctor@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });

        UserResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("doctor@example.com", response.getEmail());
        assertEquals(Role.DOCTOR, response.getRole());
        assertEquals(UserStatus.ACTIVE, response.getStatus());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("doctor@example.com", savedUser.getEmail());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
        assertEquals(1, savedUser.getRoles().size());
        assertEquals(Role.DOCTOR, savedUser.getRoles().get(0).getRole());

        // Password hash assertions per T42 requirements
        String storedHash = savedUser.getPasswordHash();
        assertNotNull(storedHash);
        assertNotEquals("SecurePass123!", storedHash);
        assertNotEquals("hashed_SecurePass123!", storedHash);
        assertTrue(passwordEncoder.matches("SecurePass123!", storedHash));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringDuplicateEmail() {
        when(userRepository.existsByEmail("doctor@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Email is already registered"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenRegisteringWithAdminRole() {
        registerRequest.setRole(Role.ADMIN);

        org.springframework.security.access.AccessDeniedException exception = assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> userService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Public registration with ADMIN role is not allowed"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfullyAndReturnJwtResponse() {
        User user = new User();
        user.setId(1L);
        user.setEmail("doctor@example.com");
        user.setPasswordHash(passwordEncoder.encode("SecurePass123!"));
        user.setStatus(UserStatus.ACTIVE);
        user.addRole(Role.DOCTOR);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user.getEmail(), null));
        when(userRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        LoginResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals("mocked.jwt.token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresInSeconds());
        assertNotNull(response.getUser());
        assertEquals("doctor@example.com", response.getUser().getEmail());
        assertEquals(Role.DOCTOR, response.getUser().getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldPropagateBadCredentialsExceptionWhenPasswordIsIncorrect() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.login(loginRequest));
    }

    @Test
    void shouldPropagateDisabledExceptionWhenUserIsDisabled() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User account is disabled"));

        assertThrows(DisabledException.class, () -> userService.login(loginRequest));
    }
}
