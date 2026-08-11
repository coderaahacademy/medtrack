package com.medtrack.service;

import com.medtrack.entity.Prescription;
import com.medtrack.entity.PrescriptionFulfillment;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidStatusTransitionException;
import com.medtrack.repository.InventoryRepository;
import com.medtrack.repository.PrescriptionFulfillmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FulfillmentServiceTest {

    @Mock
    private PrescriptionFulfillmentRepository fulfillmentRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private FulfillmentService fulfillmentService;

    private PrescriptionFulfillment fulfillment;
    private Prescription prescription;

    @BeforeEach
    void setUp() {
        prescription = new Prescription();
        prescription.setId(1L);
        prescription.setStatus(PrescriptionStatus.CANCELLED);

        fulfillment = new PrescriptionFulfillment();
        fulfillment.setId(1L);
        fulfillment.setPrescription(prescription);
    }

    @Test
    void shouldRejectAcceptWhenPrescriptionIsCancelled() {
        when(fulfillmentRepository.findById(1L)).thenReturn(Optional.of(fulfillment));
        assertThrows(InvalidStatusTransitionException.class, () -> fulfillmentService.accept(1L));
    }

    @Test
    void shouldRejectRejectWhenPrescriptionIsCancelled() {
        when(fulfillmentRepository.findById(1L)).thenReturn(Optional.of(fulfillment));
        assertThrows(InvalidStatusTransitionException.class, () -> fulfillmentService.reject(1L, null));
    }

    @Test
    void shouldRejectPreparingWhenPrescriptionIsCancelled() {
        when(fulfillmentRepository.findById(1L)).thenReturn(Optional.of(fulfillment));
        assertThrows(InvalidStatusTransitionException.class, () -> fulfillmentService.preparing(1L));
    }

    @Test
    void shouldRejectReadyWhenPrescriptionIsCancelled() {
        when(fulfillmentRepository.findById(1L)).thenReturn(Optional.of(fulfillment));
        assertThrows(InvalidStatusTransitionException.class, () -> fulfillmentService.ready(1L));
    }

    @Test
    void shouldRejectCompletedWhenPrescriptionIsCancelled() {
        when(fulfillmentRepository.findById(1L)).thenReturn(Optional.of(fulfillment));
        assertThrows(InvalidStatusTransitionException.class, () -> fulfillmentService.completed(1L));
    }
}
