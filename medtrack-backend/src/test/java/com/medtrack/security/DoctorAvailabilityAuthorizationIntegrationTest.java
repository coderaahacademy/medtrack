package com.medtrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtrack.entity.Doctor;
import com.medtrack.entity.DoctorAvailability;
import com.medtrack.entity.User;
import com.medtrack.enums.Role;
import com.medtrack.enums.UserStatus;
import com.medtrack.repository.DoctorAvailabilityRepository;
import com.medtrack.repository.DoctorRepository;
import com.medtrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DoctorAvailabilityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @BeforeEach
    void cleanAvailabilityData() {
        availabilityRepository.deleteAll();
        doctorRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser(String email, Role... roles) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode("Password123!")
        );
        user.setStatus(UserStatus.ACTIVE);

        for (Role role : roles) {
            user.addRole(role);
        }

        return userRepository.save(user);
    }

    private String token(User user) {
        return jwtService.generateToken(user);
    }

    private Doctor createDoctor(
            User user,
            String name,
            String license) {

        Doctor doctor = new Doctor();

        doctor.setUser(user);
        doctor.setFullName(name);
        doctor.setLicenseNumber(license);
        doctor.setSpecialization("Cardiology");
        doctor.setPhone("+12025550102");
        doctor.setActive(true);

        return doctorRepository.save(doctor);
    }

    private String availabilityJson(
            Long doctorId,
            LocalDateTime startAt,
            LocalDateTime endAt) {

        return """
                {
                  "doctorId": %d,
                  "startAt": "%s",
                  "endAt": "%s"
                }
                """.formatted(
                doctorId,
                startAt,
                endAt
        );
    }

    private DoctorAvailability createAvailability(
            Doctor doctor,
            LocalDateTime startAt,
            LocalDateTime endAt) {

        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setDoctor(doctor);
        availability.setStartAt(startAt);
        availability.setEndAt(endAt);

        return availabilityRepository.saveAndFlush(
                availability
        );
    }

    @Test
    void doctorCanCreateOwnAvailability() throws Exception {

        User doctorUser =
                createUser(
                        "t47.create.own@example.com",
                        Role.DOCTOR
                );

        Doctor doctor =
                createDoctor(
                        doctorUser,
                        "Doctor Own",
                        "T47-OWN"
                );

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .header(
                                        "Authorization",
                                        "Bearer " + token(doctorUser)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        availabilityJson(
                                                doctor.getId(),
                                                startAt,
                                                endAt
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.doctorId")
                                .value(doctor.getId())
                );
    }

    @Test
    void doctorCannotCreateAvailabilityForAnotherDoctor()
            throws Exception {

        User doctorUserA =
                createUser(
                        "t47.create.a@example.com",
                        Role.DOCTOR
                );

        User doctorUserB =
                createUser(
                        "t47.create.b@example.com",
                        Role.DOCTOR
                );

        Doctor doctorA =
                createDoctor(
                        doctorUserA,
                        "Doctor A",
                        "T47-A"
                );

        Doctor doctorB =
                createDoctor(
                        doctorUserB,
                        "Doctor B",
                        "T47-B"
                );

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .header(
                                        "Authorization",
                                        "Bearer " + token(doctorA.getUser())
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        availabilityJson(
                                                doctorB.getId(),
                                                startAt,
                                                endAt
                                        )
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("FORBIDDEN")
                );
    }

    @Test
    void adminCanCreateAvailabilityForAnotherDoctor()
            throws Exception {

        User adminUser =
                createUser(
                        "t47.admin.create@example.com",
                        Role.ADMIN
                );

        User doctorUser =
                createUser(
                        "t47.admin.doctor@example.com",
                        Role.DOCTOR
                );

        Doctor doctor =
                createDoctor(
                        doctorUser,
                        "Doctor Admin Target",
                        "T47-ADMIN"
                );

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(2);

        LocalDateTime endAt =
                startAt.plusHours(2);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .header(
                                        "Authorization",
                                        "Bearer " + token(adminUser)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        availabilityJson(
                                                doctor.getId(),
                                                startAt,
                                                endAt
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.doctorId")
                                .value(doctor.getId())
                );
    }

    @Test
    void doctorCanReadOwnAvailability()
            throws Exception {

        User doctorUser =
                createUser(
                        "t47.read.own@example.com",
                        Role.DOCTOR
                );

        Doctor doctor =
                createDoctor(
                        doctorUser,
                        "Doctor Read Own",
                        "T47-READ-OWN"
                );

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        createAvailability(
                doctor,
                startAt,
                endAt
        );

        mockMvc.perform(
                        get(
                                "/api/doctor-availability/doctor/{doctorId}",
                                doctor.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(doctorUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId")
                        .value(doctor.getId()));
    }

    @Test
    void doctorCannotReadAnotherDoctorsAvailability()
            throws Exception {

        User doctorUserA =
                createUser(
                        "t47.read.a@example.com",
                        Role.DOCTOR
                );

        User doctorUserB =
                createUser(
                        "t47.read.b@example.com",
                        Role.DOCTOR
                );

        Doctor doctorA =
                createDoctor(
                        doctorUserA,
                        "Doctor Read A",
                        "T47-READ-A"
                );

        Doctor doctorB =
                createDoctor(
                        doctorUserB,
                        "Doctor Read B",
                        "T47-READ-B"
                );

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        createAvailability(
                doctorB,
                startAt,
                endAt
        );

        mockMvc.perform(
                        get(
                                "/api/doctor-availability/doctor/{doctorId}",
                                doctorB.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(doctorA.getUser())
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("FORBIDDEN")
                );
    }

    @Test
    void patientCanReadActiveDoctorAvailability()
            throws Exception {

        User doctorUser =
                createUser(
                        "t47.patient.doctor@example.com",
                        Role.DOCTOR
                );

        User patientUser =
                createUser(
                        "t47.patient@example.com",
                        Role.PATIENT
                );

        Doctor doctor =
                createDoctor(
                        doctorUser,
                        "Doctor Patient Read",
                        "T47-PATIENT-READ"
                );

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        createAvailability(
                doctor,
                startAt,
                endAt
        );

        mockMvc.perform(
                        get(
                                "/api/doctor-availability/doctor/{doctorId}",
                                doctor.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(patientUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].doctorId")
                                .value(doctor.getId())
                );
    }

    @Test
    void patientCannotCreateAvailability()
            throws Exception {

        User patientUser =
                createUser(
                        "t47.patient.create@example.com",
                        Role.PATIENT
                );

        User doctorUser =
                createUser(
                        "t47.patient.target@example.com",
                        Role.DOCTOR
                );

        Doctor doctor =
                createDoctor(
                        doctorUser,
                        "Doctor Patient Target",
                        "T47-PATIENT-CREATE"
                );

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .header(
                                        "Authorization",
                                        "Bearer " + token(patientUser)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        availabilityJson(
                                                doctor.getId(),
                                                startAt,
                                                endAt
                                        )
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("FORBIDDEN")
                );
    }

    @Test
    void doctorCanDeleteOwnAvailability()
            throws Exception {

        User doctorUser =
                createUser(
                        "t47.delete.own@example.com",
                        Role.DOCTOR
                );

        Doctor doctor =
                createDoctor(
                        doctorUser,
                        "Doctor Delete Own",
                        "T47-DELETE-OWN"
                );

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        DoctorAvailability availability =
                createAvailability(
                        doctor,
                        startAt,
                        endAt
                );

        mockMvc.perform(
                        delete(
                                "/api/doctor-availability/{id}",
                                availability.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(doctorUser)
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void doctorCannotDeleteAnotherDoctorsAvailability()
            throws Exception {

        User doctorUserA =
                createUser(
                        "t47.delete.a@example.com",
                        Role.DOCTOR
                );

        User doctorUserB =
                createUser(
                        "t47.delete.b@example.com",
                        Role.DOCTOR
                );

        Doctor doctorA =
                createDoctor(
                        doctorUserA,
                        "Doctor Delete A",
                        "T47-DELETE-A"
                );

        Doctor doctorB =
                createDoctor(
                        doctorUserB,
                        "Doctor Delete B",
                        "T47-DELETE-B"
                );

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        DoctorAvailability availability =
                createAvailability(
                        doctorB,
                        startAt,
                        endAt
                );

        mockMvc.perform(
                        delete(
                                "/api/doctor-availability/{id}",
                                availability.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(doctorUserA)
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("FORBIDDEN")
                );
    }

    @Test
    void adminCanDeleteAnotherDoctorsAvailability()
            throws Exception {

        User adminUser =
                createUser(
                        "t47.admin.delete@example.com",
                        Role.ADMIN
                );

        User doctorUser =
                createUser(
                        "t47.admin.delete.doctor@example.com",
                        Role.DOCTOR
                );

        Doctor doctor =
                createDoctor(
                        doctorUser,
                        "Doctor Admin Delete",
                        "T47-ADMIN-DELETE"
                );

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        DoctorAvailability availability =
                createAvailability(
                        doctor,
                        startAt,
                        endAt
                );

        mockMvc.perform(
                        delete(
                                "/api/doctor-availability/{id}",
                                availability.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token(adminUser)
                                )
                )
                .andExpect(status().isNoContent());
    }
}
