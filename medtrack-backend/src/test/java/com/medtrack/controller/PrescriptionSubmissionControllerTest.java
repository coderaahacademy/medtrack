package com.medtrack.controller;

import com.medtrack.dto.SendToPharmacyResponse;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidStatusTransitionException;
import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.service.PrescriptionSubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PrescriptionSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrescriptionSubmissionService submissionService;

    @Test
    void shouldReturn201WhenPrescriptionIsSent() throws Exception {
        SendToPharmacyResponse response =
                new SendToPharmacyResponse();

        response.setFulfillmentId(10L);
        response.setPrescriptionId(1L);
        response.setPharmacyId(2L);
        response.setPrescriptionStatus(
                PrescriptionStatus.SENT_TO_PHARMACY
        );
        response.setFulfillmentStatus(
                FulfillmentStatus.PENDING
        );
        response.setRequestedAt(
                LocalDateTime.of(2026, 8, 6, 21, 0)
        );

        when(submissionService.sendToPharmacy(1L, 2L))
                .thenReturn(response);

        mockMvc.perform(
                        post(
                                "/api/prescriptions/{prescriptionId}/send-to-pharmacy",
                                1L
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "pharmacyId": 2
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fulfillmentId").value(10))
                .andExpect(jsonPath("$.prescriptionId").value(1))
                .andExpect(jsonPath("$.pharmacyId").value(2))
                .andExpect(
                        jsonPath("$.prescriptionStatus")
                                .value("SENT_TO_PHARMACY")
                )
                .andExpect(
                        jsonPath("$.fulfillmentStatus")
                                .value("PENDING")
                )
                .andExpect(jsonPath("$.requestedAt").exists());
    }

    @Test
    void shouldReturn400WhenPharmacyIdIsMissing() throws Exception {
        mockMvc.perform(
                        post(
                                "/api/prescriptions/{prescriptionId}/send-to-pharmacy",
                                1L
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.pharmacyId")
                                .value("Pharmacy ID is required")
                );
    }

    @Test
    void shouldReturn400WhenPharmacyIdIsNotPositive() throws Exception {
        mockMvc.perform(
                        post(
                                "/api/prescriptions/{prescriptionId}/send-to-pharmacy",
                                1L
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "pharmacyId": 0
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.pharmacyId")
                                .value("Pharmacy ID must be a valid number")
                );
    }

    @Test
    void shouldReturn404WhenPrescriptionDoesNotExist() throws Exception {
        when(submissionService.sendToPharmacy(99L, 2L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Prescription not found with id: 99"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/prescriptions/{prescriptionId}/send-to-pharmacy",
                                99L
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "pharmacyId": 2
                                        }
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Prescription not found with id: 99")
                );
    }

    @Test
    void shouldReturn409ForDuplicateSubmission() throws Exception {
        when(submissionService.sendToPharmacy(1L, 2L))
                .thenThrow(
                        new InvalidStatusTransitionException(
                                "Prescription has already been sent to a pharmacy"
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/prescriptions/{prescriptionId}/send-to-pharmacy",
                                1L
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "pharmacyId": 2
                                        }
                                        """)
                )
                .andExpect(status().isConflict());
    }
}
