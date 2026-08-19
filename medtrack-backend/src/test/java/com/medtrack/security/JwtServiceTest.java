package com.medtrack.security;

import com.medtrack.entity.User;
import com.medtrack.entity.UserRole;
import com.medtrack.enums.Role;
import com.medtrack.enums.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // 512-bit Base64 secret key for unit testing
    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1tZWR0cmFjay1hdXRoZW50aWNhdGlvbi10ZXN0aW5nMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";
    private static final long TEST_EXPIRATION_MS = 3600000; // 1 hour

    private JwtService jwtService;
    private User testUser;
    private MedTrackUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, TEST_EXPIRATION_MS);

        testUser = new User();
        testUser.setId(42L);
        testUser.setEmail("doctor@example.com");
        testUser.setPasswordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        testUser.setStatus(UserStatus.ACTIVE);

        UserRole role1 = new UserRole();
        role1.setUser(testUser);
        role1.setRole(Role.DOCTOR);
        testUser.getRoles().add(role1);

        UserRole role2 = new UserRole();
        role2.setUser(testUser);
        role2.setRole(Role.ADMIN);
        testUser.getRoles().add(role2);

        userDetails = new MedTrackUserDetails(testUser);
    }

    @Test
    void shouldGenerateValidTokenWithCorrectSubjectAndClaims() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals("doctor@example.com", jwtService.extractUsername(token));
        assertEquals(42L, jwtService.extractUserId(token));

        List<String> roles = jwtService.extractRoles(token);
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_DOCTOR"));
        assertTrue(roles.contains("ROLE_ADMIN"));

        // Verify token does not contain password or passwordHash in claims
        Claims claims = jwtService.extractAllClaims(token);
        assertNull(claims.get("password"));
        assertNull(claims.get("passwordHash"));
    }

    @Test
    void shouldValidateTokenCorrectly() {
        String token = jwtService.generateToken(testUser);

        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void shouldRejectTokenWhenUsernameDoesNotMatch() {
        String token = jwtService.generateToken(testUser);

        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setEmail("patient@example.com");
        otherUser.setPasswordHash("somehash");
        otherUser.setStatus(UserStatus.ACTIVE);

        MedTrackUserDetails otherDetails = new MedTrackUserDetails(otherUser);

        assertFalse(jwtService.isTokenValid(token, otherDetails));
    }

    @Test
    void shouldThrowExpiredJwtExceptionForExpiredToken() {
        // Create JwtService with -1000ms expiration to create an already-expired token
        JwtService expiredJwtService = new JwtService(TEST_SECRET, -1000);
        String expiredToken = expiredJwtService.generateToken(testUser);

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(expiredToken));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken(testUser);
        // Tamper with the token string
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertThrows(JwtException.class, () -> jwtService.extractUsername(tamperedToken));
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        String differentSecret = "YW5vdGhlci1zZWNyZXQta2V5LWZvci1tZWR0cmFjay1hdXRoZW50aWNhdGlvbi10ZXN0aW5nMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";
        JwtService otherJwtService = new JwtService(differentSecret, TEST_EXPIRATION_MS);

        String token = otherJwtService.generateToken(testUser);

        assertThrows(JwtException.class, () -> jwtService.extractUsername(token));
    }
}
