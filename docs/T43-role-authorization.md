# T43 — Role-Based Authorization and Resource Ownership Guide

## 1. Overview: What T42 Solved vs. What T43 Solves

### T42: Authentication ("Who are you?")
In ticket T42, secure JWT authentication with BCrypt password hashing was introduced. Authentication verifies the caller's identity:
- Verifies credentials (email and password).
- Issues signed stateless JSON Web Tokens (JWT).
- Validates the `Authorization: Bearer <token>` header on incoming requests.
- Loads user account details and authorities into Spring Security's `SecurityContextHolder`.
- Answers the fundamental question: **"Who is the caller?"**

### T43: Authorization ("What are you allowed to do?")
Authentication alone is not sufficient to protect medical data. Once an authenticated identity is established, the application must determine whether that specific identity has permission to perform an operation on a particular resource.
- Enforces role-based permissions (coarse-grained access control).
- Enforces resource ownership and relational access rules (fine-grained access control).
- Answers the fundamental question: **"Is this authenticated caller allowed to perform this operation on this specific resource?"**

---

## 2. Role-Based Authorization in MedTrack

MedTrack defines four core business roles:
- `ROLE_PATIENT`: Individuals receiving healthcare services. Can manage their personal profile, view their own prescriptions, timeline, and visits.
- `ROLE_DOCTOR`: Licensed medical professionals. Can manage their doctor profile, issue prescriptions, record visits, add clinical notes, and manage medical reports.
- `ROLE_PHARMACY`: Licensed pharmacy representatives. Can manage their pharmacy profile, pharmacy inventory, and fulfill prescription orders assigned to their pharmacy.
- `ROLE_ADMIN`: Platform administrators. Can view global directories, manage medication catalogs, and manage administrative settings.

---

## 3. Resource Ownership: Why Roles Alone Are Not Enough

Having the correct role does **not** grant universal access to all resources associated with that role.

### The Problem: Horizontal Privilege Escalation
Consider two patients:
- Patient A (`users.id = 1`, `patients.id = 10`)
- Patient B (`users.id = 2`, `patients.id = 20`)

Both Patient A and Patient B have the authority `ROLE_PATIENT`.
If the application only checks `hasRole('PATIENT')`:
- Patient A could read Patient B's medical timeline, prescriptions, and personal contact details simply by requesting `GET /api/patients/20`.
- Pharmacy A could modify Pharmacy B's inventory levels or process orders belonging to Pharmacy B.
- Doctor A could issue prescriptions falsely attributing Doctor B as the prescribing clinician.

### The Solution: Resource Ownership Verification
T43 strictly enforces that role checks and resource ownership checks work together:
```
Authorized = HasRequiredRole(caller) AND OwnsResourceOrAuthorizedRelation(caller, resourceId)
```

---

## 4. Database Ownership Models

### Direct Profile Ownership
Profiles in MedTrack maintain a strict one-to-one relationship with the authenticated user entity:

1. **Patient Profile**:
   `User (users.id)` $\leftarrow$ (1:1) $\rightarrow$ `Patient (patients.user_id)`
2. **Doctor Profile**:
   `User (users.id)` $\leftarrow$ (1:1) $\rightarrow$ `Doctor (doctors.user_id)`
3. **Pharmacy Profile (added in T43)**:
   `User (users.id)` $\leftarrow$ (1:1) $\rightarrow$ `Pharmacy (pharmacies.user_id)`

### Nested & Relational Ownership
Clinical resources link to owning profiles and derive access through relational paths:

- **Prescriptions**:
  - `User` $\rightarrow$ `Doctor` $\rightarrow$ `Prescription` (Prescribing doctor ownership)
  - `User` $\rightarrow$ `Patient` $\rightarrow$ `Prescription` (Patient ownership)
  - `User` $\rightarrow$ `Pharmacy` $\rightarrow$ `PrescriptionFulfillment` $\rightarrow$ `Prescription` (Assigned pharmacy access)
- **Fulfillments**:
  - `User` $\rightarrow$ `Pharmacy` $\rightarrow$ `PrescriptionFulfillment` (Pharmacy ownership of order processing)
- **Visits & Notes**:
  - `User` $\rightarrow$ `Doctor` $\rightarrow$ `Visit` (Treating doctor ownership)
  - `User` $\rightarrow$ `Patient` $\rightarrow$ `Visit` (Visit patient access)
  - `User` $\rightarrow$ `Doctor` $\rightarrow$ `Patient.familyDoctor` $\rightarrow$ `Visit` (Assigned family doctor access)

---

## 5. Why Client-Supplied IDs Cannot Be Trusted

A critical security principle in REST APIs is that request bodies, path parameters, and query parameters are **untrusted client inputs**.

- A request body containing `{"userId": 42}` does **not** mean the caller is user 42.
- A request body containing `{"doctorId": 12}` does **not** prove the caller is Doctor 12.
- Path variable `/api/patients/17` does **not** establish that the caller owns Patient 17.

### The Rule
Always resolve the authoritative identity from the Spring Security `SecurityContextHolder` (`MedTrackUserDetails.id`), then verify via database relationships that the target entity's `user.id` matches the authenticated `users.id`.

---

## 6. HTTP Status Code Semantics: 401 vs. 403 vs. 404

Maintaining strict status code semantics is vital for security and API predictability:

| Status Code | Meaning | Example Scenario |
| :--- | :--- | :--- |
| **401 Unauthorized** | The caller is **not authenticated** (missing, invalid, or expired JWT). | Calling `GET /api/patients/1` without `Authorization` header. |
| **403 Forbidden** | The caller **is authenticated**, but lacks permission for this action or resource. | Patient A calling `GET /api/patients/2` (Patient B's ID). |
| **404 Not Found** | The resource truly does not exist in the database. | Calling `GET /api/patients/99999` where ID 99999 is absent. |

---

## 7. Spring Security Method Authorization Architecture

T43 activates declarative method security in Spring Security via:

### 1. `@EnableMethodSecurity`
Annotated on `SecurityConfig` to enable `@PreAuthorize`, `@PostAuthorize`, and `@Secured` annotations throughout the application.

### 2. `@PreAuthorize`
Evaluates SpEL (Spring Expression Language) expressions before invoking the controller or service method:
- Role check: `@PreAuthorize("hasRole('ADMIN')")`
- Combined check: `@PreAuthorize("@authz.canAccessPatient(#id)")`
- Creation check: `@PreAuthorize("@authz.canCreateDoctor(#request.userId)")`

### 3. `ResourceAuthorizationService` (`@Service("authz")`)
The single authoritative service encapsulating all ownership and access policies:
- `currentUserId()`: Extracts `MedTrackUserDetails.id` from `SecurityContextHolder`.
- `ownsPatient(patientId)`: Verifies if patient's `user.id` equals `currentUserId()`.
- `ownsDoctor(doctorId)`: Verifies if doctor's `user.id` equals `currentUserId()`.
- `ownsPharmacy(pharmacyId)`: Verifies if pharmacy's `user.id` equals `currentUserId()`.
- `canAccessPatient(patientId)`: Returns true if caller is ADMIN, owning patient, or assigned family doctor.
- `canCreatePrescription(doctorId)`: Returns true if caller is a DOCTOR and owns `doctorId`.
- `canReadPrescription(prescriptionId)`: Returns true if caller is ADMIN, prescription patient, prescribing doctor, or assigned pharmacy.
- `canCancelPrescription(prescriptionId)` / `canSendToPharmacy(prescriptionId)`: Returns true if caller is the prescribing DOCTOR.
- `canModifyFulfillment(fulfillmentId)`: Returns true if caller is the assigned PHARMACY.
- `canAddVisitNotes(visitId)`: Returns true if caller is the treating DOCTOR.
- `canReadVisitNotes(visitId)`: Returns true if caller is ADMIN, visit patient, treating doctor, or family doctor.
- `canCreateMedicalReport(doctorId)`: Returns true if caller is ADMIN or prescribing DOCTOR for their own profile.

---

## 8. Endpoint Authorization Matrix

| Endpoint | Method | Allowed Roles / Ownership Rule |
| :--- | :--- | :--- |
| `/users/register` | `POST` | Public (except requesting `Role.ADMIN` which returns 403) |
| `/users/login` | `POST` | Public |
| `/api/doctors` | `GET` | All authenticated roles |
| `/api/doctors/{id}` | `GET` | All authenticated roles |
| `/api/doctors` | `POST` | DOCTOR for own `userId`, ADMIN |
| `/api/doctors/{id}` | `PUT`, `DELETE` | Doctor owner, ADMIN |
| `/api/patients` | `GET` | ADMIN only |
| `/api/patients/{id}` | `GET` | Patient owner, assigned family doctor, ADMIN |
| `/api/patients` | `POST` | PATIENT for own `userId`, ADMIN |
| `/api/patients/{id}` | `PUT`, `DELETE` | Patient owner, ADMIN |
| `/api/patients/{id}/family-doctor` | `PATCH` | Patient owner, ADMIN |
| `/api/patients/{id}/prescriptions` | `GET` | Patient owner, assigned family doctor, ADMIN |
| `/api/patients/{id}/timeline` | `GET` | Patient owner, assigned family doctor, ADMIN |
| `/api/pharmacies` | `GET`, `GET /{id}` | All authenticated roles |
| `/api/pharmacies` | `POST` | PHARMACY for own `userId`, ADMIN |
| `/api/pharmacies/{id}` | `PUT` | Pharmacy owner, ADMIN |
| `/api/pharmacies/{id}/deactivate` | `PATCH` | ADMIN only |
| `/api/pharmacies/{id}/inventory/**`| `GET`, `POST`, `PUT` | Pharmacy owner, ADMIN |
| `/api/medications` | `GET`, `GET /search`, `GET /{id}` | All authenticated roles |
| `/api/medications/**` (mutations)| `POST`, `PUT`, `PATCH` | ADMIN only |
| `/api/prescriptions` | `GET` | ADMIN only |
| `/api/prescriptions/{id}` | `GET` | Prescribing doctor, patient owner, assigned pharmacy, ADMIN |
| `/api/prescriptions` | `POST` | Prescribing DOCTOR with own `doctorId` |
| `/api/prescriptions/{id}/cancel` | `PATCH` | Prescribing DOCTOR only |
| `/api/prescriptions/{id}/send-to-pharmacy` | `POST` | Prescribing DOCTOR only |
| `/api/prescriptions/{id}/status` | `PATCH` | ADMIN only |
| `/api/prescriptions/{id}/audit-history` | `GET` | Prescribing doctor, patient owner, assigned pharmacy, ADMIN |
| `/api/fulfillments/{id}/*` | `PATCH` | Assigned PHARMACY only |
| `/api/visits` | `POST` | DOCTOR with own `doctorId` |
| `/api/visits/patient/{patientId}` | `GET` | Patient owner, assigned family doctor, ADMIN |
| `/api/visits/{id}/notes` | `POST` | Treating DOCTOR only |
| `/api/visits/{id}/notes` | `GET` | Visit patient, treating doctor, family doctor, ADMIN |
| `/medical-reports` | `POST` | DOCTOR with own `doctorId`, ADMIN |

---

## 9. Pharmacy Ownership & Entity Model Changes

In T43, the `Pharmacy` entity was brought into alignment with `Patient` and `Doctor` profiles:
- Added `@OneToOne` join column `user_id` on `Pharmacy` (unique, non-null).
- Added inverse `@OneToOne` relationship on `User`.
- Added `findByUserId(Long userId)` and `existsByUserId(Long userId)` to `PharmacyRepository`.
- Created `CreatePharmacyRequest` containing `userId` and validation rules.
- Ownership is immutable: `UpdatePharmacyRequest` / `PUT /api/pharmacies/{id}` does not permit reassigning `user_id`.

---

## 10. Prevention of Public ADMIN Self-Registration

Public registration via `POST /users/register` is open for new users to register as `PATIENT`, `DOCTOR`, or `PHARMACY`.
However, requesting `Role.ADMIN` in the registration payload is rejected immediately with a standardized **403 Forbidden** error response. Administrative accounts must be created through secure administrative provisioning.

---

## 11. Multi-Role User Support

The MedTrack identity architecture allows a user to hold multiple roles (e.g. `ROLE_DOCTOR` and `ROLE_ADMIN` simultaneously).
- Role checks inspect the full list of granted authorities (`auth.getAuthorities()`), not merely a single primary role.
- Having administrative authority does not grant clinical prescribing privileges; clinical actions (e.g., issuing a prescription) require possessing `ROLE_DOCTOR` and acting on one's own `Doctor` profile.

---

## 12. Standardized 403 Response Contract

Both filter-level authorization denials and method-level `AccessDeniedException` produce the standardized T41 `ErrorResponseDto` format:
```json
{
  "timestamp": "2026-08-21T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "code": "FORBIDDEN",
  "message": "You do not have permission to access this resource",
  "path": "/api/patients/2",
  "fieldErrors": []
}
```

Components implementing this:
1. `CustomAccessDeniedHandler`: Implements Spring Security's `AccessDeniedHandler` for filter-chain denials.
2. `GlobalExceptionHandler`: Annotates `@ExceptionHandler(AccessDeniedException.class)` for controller/service denials.

---

## 13. Example HTTP Requests & Responses

### Case 1: Unauthenticated Request $\rightarrow$ 401 Unauthorized
```http
GET /api/patients/1 HTTP/1.1
Host: localhost:8080
```
```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "timestamp": "2026-08-21T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "code": "UNAUTHORIZED",
  "message": "Full authentication is required to access this resource",
  "path": "/api/patients/1",
  "fieldErrors": []
}
```

### Case 2: Patient Accessing Own Profile $\rightarrow$ 200 OK
```http
GET /api/patients/1 HTTP/1.1
Host: localhost:8080
Authorization: Bearer <JWT_FOR_PATIENT_1>
```
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "userId": 10,
  "fullName": "Alice Smith",
  "birthDate": "1990-01-01",
  "gender": "F",
  "bloodGroup": "O+",
  "phone": "+12025550101",
  "address": "123 Main St"
}
```

### Case 3: Patient Attempting to Access Another Patient's Profile $\rightarrow$ 403 Forbidden
```http
GET /api/patients/2 HTTP/1.1
Host: localhost:8080
Authorization: Bearer <JWT_FOR_PATIENT_1>
```
```http
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "timestamp": "2026-08-21T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "code": "FORBIDDEN",
  "message": "You do not have permission to access this resource",
  "path": "/api/patients/2",
  "fieldErrors": []
}
```

---

## 14. Testing Strategy

The authorization test architecture is implemented in `AuthorizationIntegrationTest`:
- Fully end-to-end `@SpringBootTest` with `@AutoConfigureMockMvc`.
- Uses real JWT tokens generated via `JwtService` and real DB-persisted user/entity relationships.
- Covers:
  - Authentication regression (401 on missing JWT).
  - Public registration rules (PATIENT, DOCTOR, PHARMACY allowed; ADMIN blocked with 403).
  - Standardized 403 payload contract verification.
  - Cross-user attacks across Patients, Doctors, Pharmacies, Prescriptions, Fulfillments, Visits, and Reports.
  - Multi-role evaluation and role vs. ownership distinctions.

---

## 15. Things Intentionally NOT Implemented in T43

The following items belong to future roadmap tickets and are intentionally excluded from T43:
- **T44**: Inventory concurrency hardening and pessimistic locking.
- **T45**: Partial fulfillment and `dispensed_quantity` redesign.
- **T46**: Pharmacy fulfillment queue implementation.
- **T47**: Doctor availability scheduling.
- **T48–T50**: Appointments lifecycle and appointment-to-visit workflow.
- **T51**: Visit workflow redesign.
- **T52**: Medical history expansion.
- Refresh tokens, OAuth2, external identity providers.
