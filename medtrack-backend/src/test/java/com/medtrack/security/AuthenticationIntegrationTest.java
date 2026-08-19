package com.medtrack.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtrack.dto.LoginRequest;
import com.medtrack.dto.RegisterUserRequest;
import com.medtrack.entity.User;
import com.medtrack.entity.UserRole;
import com.medtrack.enums.Role;
import com.medtrack.enums.UserStatus;
import com.medtrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // ==========================================
    // REGISTRATION TESTS
    // ==========================================

    @Test
    void shouldRegisterUserSuccessfullyWithBCryptHash() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("alice@example.com");
        request.setPassword("SecretPass123!");
        request.setRole(Role.DOCTOR);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("DOCTOR"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        // Verify database persistence and BCrypt encoding
        User user = userRepository.findByEmail("alice@example.com").orElseThrow();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getPasswordHash());
        assertNotEquals("SecretPass123!", user.getPasswordHash());
        assertNotEquals("hashed_SecretPass123!", user.getPasswordHash());
        assertTrue(passwordEncoder.matches("SecretPass123!", user.getPasswordHash()));
        assertTrue(user.hasRole(Role.DOCTOR));
    }

    @Test
    void shouldReturn400WhenRegisteringWithWeakPassword() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("weak@example.com");
        request.setPassword("simple"); // Missing length, uppercase, number, special char
        request.setRole(Role.PATIENT);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldReturn400WhenRegisteringDuplicateEmail() throws Exception {
        User user = new User();
        user.setEmail("existing@example.com");
        user.setPasswordHash(passwordEncoder.encode("SecretPass123!"));
        user.setStatus(UserStatus.ACTIVE);
        user.addRole(Role.PATIENT);
        userRepository.save(user);

        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("existing@example.com");
        request.setPassword("SecretPass123!");
        request.setRole(Role.PATIENT);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Email is already registered: existing@example.com"));
    }

    // ==========================================
    // LOGIN TESTS
    // ==========================================

    @Test
    void shouldLoginSuccessfullyAndReturnJwtWithAllRolesAndUserId() throws Exception {
        User user = new User();
        user.setEmail("multi.role@example.com");
        user.setPasswordHash(passwordEncoder.encode("SecretPass123!"));
        user.setStatus(UserStatus.ACTIVE);
        user.addRole(Role.DOCTOR);
        user.addRole(Role.ADMIN);
        User savedUser = userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("multi.role@example.com");
        request.setPassword("SecretPass123!");

        MvcResult result = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").isNumber())
                .andExpect(jsonPath("$.user.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.user.email").value("multi.role@example.com"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = responseNode.get("accessToken").asText();

        // Verify token payload
        assertEquals("multi.role@example.com", jwtService.extractUsername(token));
        assertEquals(savedUser.getId(), jwtService.extractUserId(token));

        List<String> roles = jwtService.extractRoles(token);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_DOCTOR"));
        assertTrue(roles.contains("ROLE_ADMIN"));

        // Verify sensitive data is not leaked in token claims
        assertNull(jwtService.extractAllClaims(token).get("password"));
        assertNull(jwtService.extractAllClaims(token).get("passwordHash"));
    }

    @Test
    void shouldReturn401WhenLoginWithWrongPassword() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash(passwordEncoder.encode("CorrectPass123!"));
        user.setStatus(UserStatus.ACTIVE);
        user.addRole(Role.PATIENT);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("WrongPassword123!");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/users/login"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void shouldReturn401WhenLoginWithNonExistentEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("SecretPass123!");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/users/login"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void shouldReturn401WhenLoginWithDisabledUser() throws Exception {
        User user = new User();
        user.setEmail("disabled@example.com");
        user.setPasswordHash(passwordEncoder.encode("SecretPass123!"));
        user.setStatus(UserStatus.DISABLED);
        user.addRole(Role.PATIENT);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("disabled@example.com");
        request.setPassword("SecretPass123!");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.path").value("/users/login"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void shouldReturn400ValidationFailedWhenLoginEmailIsInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email");
        request.setPassword("SecretPass123!");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    // ==========================================
    // PROTECTED ENDPOINT & TOKEN VALIDATION TESTS
    // ==========================================

    @Test
    void shouldReturn401WhenAccessingProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/prescriptions/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.path").value("/api/prescriptions/1"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void shouldReturn401WhenAccessingProtectedEndpointWithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/prescriptions/1")
                        .header("Authorization", "Bearer not.a.valid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.path").value("/api/prescriptions/1"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void shouldReturn401WhenAccessingProtectedEndpointWithInvalidSignature() throws Exception {
        String differentSecret = "YW5vdGhlci1zZWNyZXQta2V5LWZvci1tZWR0cmFjay1hdXRoZW50aWNhdGlvbi10ZXN0aW5nMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";
        JwtService foreignJwtService = new JwtService(differentSecret, 3600000);

        User user = new User();
        user.setId(10L);
        user.setEmail("user@example.com");
        user.setPasswordHash("hash");
        user.setStatus(UserStatus.ACTIVE);
        user.addRole(Role.DOCTOR);

        String invalidSigToken = foreignJwtService.generateToken(user);

        mockMvc.perform(get("/api/prescriptions/1")
                        .header("Authorization", "Bearer " + invalidSigToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldReturn401WhenAccessingProtectedEndpointWithExpiredToken() throws Exception {
        JwtService expiredJwtService = new JwtService(
                "dGVzdC1zZWNyZXQta2V5LWZvci1tZWR0cmFjay1hdXRoZW50aWNhdGlvbi10ZXN0aW5nMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=",
                -1000
        );

        User user = new User();
        user.setEmail("expired@example.com");
        user.setPasswordHash(passwordEncoder.encode("SecretPass123!"));
        user.setStatus(UserStatus.ACTIVE);
        user.addRole(Role.DOCTOR);
        User savedUser = userRepository.save(user);

        String expiredToken = expiredJwtService.generateToken(savedUser);

        mockMvc.perform(get("/api/prescriptions/1")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldReturn401WhenTokenUserHasBeenDisabledAfterTokenIssuance() throws Exception {
        User user = new User();
        user.setEmail("active-then-disabled@example.com");
        user.setPasswordHash(passwordEncoder.encode("SecretPass123!"));
        user.setStatus(UserStatus.ACTIVE);
        user.addRole(Role.DOCTOR);
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        // Now disable user account in database
        savedUser.setStatus(UserStatus.DISABLED);
        userRepository.save(savedUser);

        // Request should be rejected because account is now DISABLED
        mockMvc.perform(get("/api/prescriptions/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldAllowAccessToProtectedEndpointWithValidToken() throws Exception {
        User user = new User();
        user.setEmail("authenticated@example.com");
        user.setPasswordHash(passwordEncoder.encode("SecretPass123!"));
        user.setStatus(UserStatus.ACTIVE);
        user.addRole(Role.DOCTOR);
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        // Access protected endpoint with valid token. It passes authentication layer.
        // Even if the resource with ID 99999 is not found (404), it confirms authentication succeeded (not 401).
        mockMvc.perform(get("/api/prescriptions/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus()));
    }

    @Test
    void shouldAllowAccessToOpenApiDocumentationWithoutToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
