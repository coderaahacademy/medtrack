# T42 — Secure Authentication with Spring Security and JWT

## 1. Background: What Was Wrong Before T42
Prior to ticket T42, the MedTrack backend lacked a real security architecture:
- **Insecure Password Storage:** Passwords were stored using a placeholder pseudo-hash: `"hashed_" + rawPassword`. This meant anyone with database read access could trivially obtain the cleartext passwords.
- **Manual Password Comparison:** The login endpoint performed string equality checks on the pseudo-hash instead of cryptographic verification.
- **Unprotected APIs:** Business API endpoints (e.g., prescriptions, patients, medications) had no authentication requirement, allowing anonymous HTTP clients to access and manipulate medical records.
- **No Bearer Token Infrastructure:** The login endpoint returned a user summary without an access token to authorize subsequent API calls.

---

## 2. What T42 Adds
T42 introduces enterprise-grade, stateless authentication using industry standards:
- **Spring Security 6 (Spring Boot 3.4.1):** Manages the security filter chain, authentication providers, and authorization contexts.
- **BCrypt Password Hashing (`BCryptPasswordEncoder`):** Uses an adaptive, salted one-way hashing function to securely store user credentials.
- **Stateless JSON Web Tokens (JWT via JJWT 0.12.6):** Generates digitally signed HMAC-SHA256 bearer tokens upon login.
- **Stateless Request Filtering (`JwtAuthenticationFilter`):** Intercepts requests, validates token signatures and expiration, verifies account active status, and establishes the Spring Security context.
- **Standardized Error Responses (`CustomAuthenticationEntryPoint`):** Ensures all authentication failures return the standardized T41 `ErrorResponseDto` JSON format with HTTP 401.

---

## 3. Registration Flow

```
HTTP Request (POST /users/register)
  │
  ▼
UserController
  │  (Validates @Valid RegisterUserRequest: email format, 8+ chars, upper, lower, digit, special)
  ▼
UserService
  │  1. Verifies email uniqueness via UserRepository.existsByEmail()
  │  2. Hashes raw password using PasswordEncoder.encode() (BCrypt)
  │  3. Sets User.status = UserStatus.ACTIVE
  │  4. Creates UserRole record for the requested Role
  ▼
UserRepository
  │  Persists User entity to 'users' and UserRole to 'user_roles' table
  ▼
UserResponse DTO
  │  Returns safe user representation (id, email, role, status, createdAt)
  │  (NEVER returns raw password or passwordHash)
  ▼
HTTP 201 Created
```

---

## 4. Login Flow

```
HTTP Request (POST /users/login)
  │  Body: { "email": "doctor@example.com", "password": "SecretPassword123!" }
  ▼
UserController
  │
  ▼
UserService
  │
  ├──► AuthenticationManager.authenticate(UsernamePasswordAuthenticationToken)
  │      │
  │      ├──► DaoAuthenticationProvider
  │      │      ├──► MedTrackUserDetailsService.loadUserByUsername(email)
  │      │      │      └──► UserRepository.findByEmail(email)
  │      │      └──► PasswordEncoder.matches(rawPassword, storedBCryptHash)
  │      │             (Checks password and checks user.isEnabled() -> ACTIVE status)
  │      │
  │      └──► Throws AuthenticationException (BadCredentialsException / DisabledException) on failure
  │
  ├──► JwtService.generateToken(user)
  │      (Signs JWT containing email subject, userId, and roles claims)
  │
  ▼
LoginResponse DTO
  │  {
  │    "success": true,
  │    "message": "Login successful",
  │    "accessToken": "<jwt-token>",
  │    "tokenType": "Bearer",
  │    "expiresInSeconds": 3600,
  │    "user": { ... }
  │  }
  ▼
HTTP 200 OK
```

---

## 5. Authenticated Request Flow

```
HTTP Request with "Authorization: Bearer <jwt-token>"
  │
  ▼
JwtAuthenticationFilter (OncePerRequestFilter)
  │  1. Extracts token from "Authorization: Bearer <token>"
  │  2. Extracts username (email) and validates signature + expiration (JwtService)
  │  3. Loads current user state from DB via MedTrackUserDetailsService
  │  4. Verifies account is ACTIVE (rejects DISABLED accounts)
  │  5. Creates UsernamePasswordAuthenticationToken with current authorities
  │  6. Sets Authentication in SecurityContextHolder.getContext()
  ▼
Spring Security AuthorizationFilter / FilterSecurityInterceptor
  │  Checks if request is authenticated
  ▼
Target Controller (e.g., PrescriptionController)
  │
  ▼
HTTP Response (e.g. 200 OK)
```

---

## 6. Core Security Concepts Explained

### Authentication vs. Authorization
- **Authentication (T42):** Verifies *identity* — "Who is making this request, and are their credentials valid?"
- **Authorization (T43):** Verifies *permissions* — "Is this authenticated user (e.g., DOCTOR, PATIENT, PHARMACY) allowed to access this specific resource or perform this action?"

### BCrypt & One-Way Password Hashing
- BCrypt is a cryptographic one-way hashing function incorporating a random per-user salt and a configurable work factor (computational cost).
- **Passwords cannot be decrypted:** When a user logs in, BCrypt computes the hash of the supplied password with the stored salt and compares hashes (`passwordEncoder.matches(raw, hash)`). The raw password is never stored or decryptable.

### JWT Signatures & Expiration
- A JSON Web Token consists of three Base64URL-encoded parts separated by dots: `Header.Payload.Signature`.
- **Signature:** Computed using HMAC-SHA256 over `Header.Payload` with the secret key `JWT_SECRET`. Any modification to the payload by a client invalidates the signature.
- **Expiration (`exp`):** Timestamp indicating when the token ceases to be valid. The server rejects tokens where `exp < currentTime`.

### Bearer Token Header
Per RFC 6750, the client transmits the access token in the HTTP `Authorization` request header formatted as:
```
Authorization: Bearer <access_token>
```

### SecurityContext & UserDetails
- `SecurityContext`: Holds the currently authenticated principal (`Authentication` object) for the current request thread.
- `UserDetails` (`MedTrackUserDetails`): Represents the user principal within Spring Security, exposing username, password hash, account status (`isEnabled`), and authorities.
- `ROLE_` Prefix: Spring Security role-based checks expect authorities to follow the standard `ROLE_<NAME>` naming convention (e.g., `ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_ADMIN`, `ROLE_PHARMACY`).

---

## 7. JWT Claims Structure

A token generated by MedTrack contains:

```json
{
  "sub": "doctor@example.com",
  "userId": 42,
  "roles": [
    "ROLE_DOCTOR",
    "ROLE_ADMIN"
  ],
  "iat": 1755619200,
  "exp": 1755622800
}
```

- `sub` (Subject): Stable user email address.
- `userId`: Numeric identifier of the User entity in the database.
- `roles`: Array of all assigned roles with the `ROLE_` prefix.
- `iat`: Timestamp when token was issued.
- `exp`: Timestamp when token expires.

---

## 8. Why Passwords Must Never Be in JWT Claims
JWT payloads are only Base64URL-encoded, not encrypted. Anyone who inspects the token (e.g., browser storage, proxies, client-side scripts, network logs) can read the payload. Therefore, sensitive data such as passwords, password hashes, and sensitive medical data must never be placed inside JWT claims.

---

## 9. Account Status: ACTIVE vs. DISABLED
- `UserStatus.ACTIVE`: The account is in good standing and permitted to authenticate and access APIs.
- `UserStatus.DISABLED`: The account has been deactivated.
  - Login attempts are rejected with HTTP 401.
  - Existing, unexpired JWT tokens for disabled users are immediately rejected on subsequent requests because `JwtAuthenticationFilter` validates the latest database account status.

---

## 10. Scope Boundary: T42 vs. T43
- **T42 (This Ticket):** Implements secure authentication (BCrypt, Spring Security, JWT, public register/login, 401 error standardization). Any active, authenticated user passes the authentication layer.
- **T43 (Upcoming Ticket):** Implements role-based access control (RBAC), method-level authorization (`@PreAuthorize`), and resource ownership (e.g. pharmacy ownership, doctor-patient relationship constraints).

---

## 11. Developer Setup & How to Run

### Step 1: Configure Environment Variable
Generate a secure 256+ bit Base64 string for local development or set an environment variable:

```bash
export JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1tZWR0cmFjay1hdXRoZW50aWNhdGlvbi10ZXN0aW5nMTIzNDU2Nzg5MDEyMzQ1Njc4OTA="
```

### Step 2: Run Application
```bash
./mvnw -f medtrack-backend/pom.xml spring-boot:run
```

---

## 12. Example cURL Commands

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "dr.smith@example.com",
    "password": "SecurePassword123!",
    "role": "DOCTOR"
  }'
```
Response: `201 Created`

### 2. Login to Obtain Access Token
```bash
curl -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "dr.smith@example.com",
    "password": "SecurePassword123!"
  }'
```
Response: `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600,
  "user": {
    "id": 1,
    "email": "dr.smith@example.com",
    "role": "DOCTOR",
    "status": "ACTIVE",
    "createdAt": "2026-08-19T16:00:00"
  }
}
```

### 3. Call a Protected Endpoint with Bearer Token
```bash
curl -X GET http://localhost:8080/api/prescriptions/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 13. Common 401 Unauthorized Scenarios

All authentication failures return the standardized T41 `ErrorResponseDto` format:
```json
{
  "timestamp": "2026-08-19T16:30:00.000",
  "status": 401,
  "error": "Unauthorized",
  "code": "UNAUTHORIZED",
  "message": "<Description>",
  "path": "/api/...",
  "fieldErrors": []
}
```

| Scenario | HTTP Status | Code | Cause |
|---|---|---|---|
| Missing token on protected endpoint | 401 | `UNAUTHORIZED` | Request omitted the `Authorization` header |
| Malformed token | 401 | `UNAUTHORIZED` | Bearer token format is invalid or corrupted |
| Invalid signature | 401 | `UNAUTHORIZED` | Token was signed with a different key or tampered |
| Expired token | 401 | `UNAUTHORIZED` | Token expiration timestamp `exp` has passed |
| Invalid credentials | 401 | `UNAUTHORIZED` | Email does not exist or password does not match |
| Disabled account | 401 | `UNAUTHORIZED` | User status is `DISABLED` in the database |

---

## 14. Testing Architecture

- `UserServiceTest`: Unit tests for password encoding, duplicate email rejection, login authentication, and DTO conversion.
- `JwtServiceTest`: Unit tests for token generation, claims extraction (`userId`, `roles`), expiration verification, tampering detection, and signature validation.
- `AuthenticationIntegrationTest`: End-to-end MockMvc integration tests for registration, login success/failure, active/disabled status enforcement, protected endpoint token validation, OpenAPI docs public access, and 401 error response shape validation.
- `PrescriptionSubmissionControllerTest` & `PrescriptionIntegrationTest`: Verified to work seamlessly with `@WithMockUser` to ensure existing domain tests pass with security enabled.

---

## 15. Architectural & Security Decisions

1. **Stateless Session Management (`SessionCreationPolicy.STATELESS`):**  
   REST APIs should be scalable across multiple instances without server session stickiness or distributed session stores.
2. **CSRF Disabled:**  
   Cross-Site Request Forgery attacks rely on browsers automatically attaching session cookies to cross-origin requests. Because MedTrack uses custom HTTP `Authorization: Bearer <token>` headers that browsers never send automatically, CSRF protection is unnecessary and disabled.
3. **BCrypt Password Hashing:**  
   BCrypt includes salting and a configurable work factor, protecting stored passwords against rainbow table lookups and brute-force attacks.
4. **Externalized JWT Secret:**  
   Signing secrets must never be hardcoded or checked into source control. MedTrack requires `JWT_SECRET` via configuration in production, while using isolated test-only properties for automated testing.
5. **Database Account Verification During Request Authentication:**  
   Rather than trusting a JWT blindly for its entire lifetime, `JwtAuthenticationFilter` checks the database for the user's latest status. If an account is deactivated (`DISABLED`), all existing tokens immediately stop working without waiting for token expiration.
