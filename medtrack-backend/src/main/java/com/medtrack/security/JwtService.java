package com.medtrack.security;

import com.medtrack.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service responsible for generating, parsing, and validating stateless JSON Web Tokens (JWT).
 * Signs tokens using HMAC-SHA256 with an externally configured secret key.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    public JwtService() {
    }

    public JwtService(String secretKey, long jwtExpirationMs) {
        this.secretKey = secretKey;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Generate a JWT for a given MedTrack User entity containing user ID and all assigned roles.
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());

        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream()
                .map(r -> "ROLE_" + r.getRole().name())
                .collect(Collectors.toList())
                : List.of();
        extraClaims.put("roles", roles);

        return buildToken(extraClaims, user.getEmail(), jwtExpirationMs);
    }

    /**
     * Generate a JWT for a given UserDetails and custom claims.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails.getUsername(), jwtExpirationMs);
    }

    /**
     * Build token with claims, subject (email), issued-at, expiration, and HMAC-SHA signing key.
     */
    public String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        long nowMillis = System.currentTimeMillis();
        Date issuedAt = new Date(nowMillis);
        Date expirationDate = new Date(nowMillis + expiration);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extract the username (subject email) from a JWT token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the user ID claim from a JWT token.
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    /**
     * Extract the roles list claim from a JWT token.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    /**
     * Extract expiration date from a JWT token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim using a claims resolver function.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse and verify the token signature and return claims payload.
     * Throws JwtException subclasses (ExpiredJwtException, SignatureException, MalformedJwtException) on errors.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate token against user details: checks matching subject email and token expiration.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username != null && username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Check if token expiration is before the current time.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * Decodes the configured secret key. Supports Base64 encoded keys as well as raw UTF-8 keys
     * (ensuring minimum HMAC-SHA256 key length of 256 bits).
     */
    private SecretKey getSigningKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT signing secret is not configured. Please set JWT_SECRET.");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException e) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
