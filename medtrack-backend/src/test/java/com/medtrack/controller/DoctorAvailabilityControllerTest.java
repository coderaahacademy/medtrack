package com.medtrack.controller;

import com.medtrack.dto.DoctorAvailabilityResponse;
import com.medtrack.security.ResourceAuthorizationService;
import com.medtrack.service.DoctorAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class DoctorAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorAvailabilityService availabilityService;

    @MockitoBean
    private ResourceAuthorizationService authz;

    @BeforeEach
    void setUp() {
        when(authz.canManageDoctorAvailability(anyLong()))
                .thenReturn(true);
    }

    @Test
    void shouldReturn201WhenAvailabilityIsValid() throws Exception {

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        DoctorAvailabilityResponse response =
                new DoctorAvailabilityResponse();

        response.setId(1L);
        response.setDoctorId(10L);
        response.setStartAt(startAt);
        response.setEndAt(endAt);

        when(availabilityService.create(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "doctorId": 10,
                                          "startAt": "%s",
                                          "endAt": "%s"
                                        }
                                        """.formatted(startAt, endAt))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.doctorId").value(10))
                .andExpect(jsonPath("$.startAt").exists())
                .andExpect(jsonPath("$.endAt").exists());
    }

    @Test
    void shouldReturn400WhenDoctorIdIsMissing() throws Exception {

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "startAt": "%s",
                                          "endAt": "%s"
                                        }
                                        """.formatted(startAt, endAt))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldReturn400WhenStartAtIsMissing() throws Exception {

        LocalDateTime endAt =
                LocalDateTime.now().plusDays(1);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "doctorId": 10,
                                          "endAt": "%s"
                                        }
                                        """.formatted(endAt))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldReturn400WhenEndAtIsMissing() throws Exception {

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "doctorId": 10,
                                          "startAt": "%s"
                                        }
                                        """.formatted(startAt))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldReturn400WhenStartAtIsNull() throws Exception {

        LocalDateTime endAt =
                LocalDateTime.now().plusDays(1);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "doctorId": 10,
                                          "startAt": null,
                                          "endAt": "%s"
                                        }
                                        """.formatted(endAt))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldReturn400WhenEndAtIsNull() throws Exception {

        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "doctorId": 10,
                                          "startAt": "%s",
                                          "endAt": null
                                        }
                                        """.formatted(startAt))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldReturn400WhenStartAtIsInThePast() throws Exception {

        LocalDateTime startAt =
                LocalDateTime.now().minusHours(1);

        LocalDateTime endAt =
                LocalDateTime.now().plusHours(1);

        mockMvc.perform(
                        post("/api/doctor-availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "doctorId": 10,
                                          "startAt": "%s",
                                          "endAt": "%s"
                                        }
                                        """.formatted(startAt, endAt))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
}
