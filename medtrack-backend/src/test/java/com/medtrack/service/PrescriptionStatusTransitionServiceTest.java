package com.medtrack.service;

import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrescriptionStatusTransitionServiceTest {

    private final PrescriptionStatusTransitionService service =
            new PrescriptionStatusTransitionService();

    @Test
    void shouldRejectDraftToAnyStatus() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.validate(
                        PrescriptionStatus.DRAFT,
                        PrescriptionStatus.ISSUED
                )
        );
    }


    @Test
    void shouldAllowIssuedToSentToPharmacy() {
        assertDoesNotThrow(() ->
                service.validate(
                        PrescriptionStatus.ISSUED,
                        PrescriptionStatus.SENT_TO_PHARMACY
                )
        );
    }

    @Test
    void shouldAllowIssuedToCancelled() {
        assertDoesNotThrow(() ->
                service.validate(
                        PrescriptionStatus.ISSUED,
                        PrescriptionStatus.CANCELLED
                )
        );
    }

    @Test
    void shouldAllowSentToPharmacyToCompleted() {
        assertDoesNotThrow(() ->
                service.validate(
                        PrescriptionStatus.SENT_TO_PHARMACY,
                        PrescriptionStatus.COMPLETED
                )
        );
    }

    @Test
    void shouldRejectCompletedToAnyStatus() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.validate(
                        PrescriptionStatus.COMPLETED,
                        PrescriptionStatus.ISSUED
                )
        );
    }

    @Test
    void shouldRejectCancelledToAnyStatus() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.validate(
                        PrescriptionStatus.CANCELLED,
                        PrescriptionStatus.ISSUED
                )
        );
    }

    @Test
    void shouldRejectSameStatus() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.validate(
                        PrescriptionStatus.ISSUED,
                        PrescriptionStatus.ISSUED
                )
        );
    }

    @Test
    void shouldRejectUndefinedTransition() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.validate(
                        PrescriptionStatus.DRAFT,
                        PrescriptionStatus.COMPLETED
                )
        );
    }

    @Test
    void shouldRejectNullCurrentStatus() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.validate(
                        null,
                        PrescriptionStatus.ISSUED
                )
        );
    }

    @Test
    void shouldRejectNullNewStatus() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.validate(
                        PrescriptionStatus.ISSUED,
                        null
                )
        );
    }

    @Test
    void shouldAllowSentToPharmacyToPartiallyFulfilled() {
        assertDoesNotThrow(() ->
                service.validate(
                        PrescriptionStatus.SENT_TO_PHARMACY,
                        PrescriptionStatus.PARTIALLY_FULFILLED
                )
        );
    }

    @Test
    void shouldAllowSentToPharmacyToCancelled() {
        assertDoesNotThrow(() ->
                service.validate(
                        PrescriptionStatus.SENT_TO_PHARMACY,
                        PrescriptionStatus.CANCELLED
                )
        );
    }

    @Test
    void shouldAllowPartiallyFulfilledToCompleted() {
        assertDoesNotThrow(() ->
                service.validate(
                        PrescriptionStatus.PARTIALLY_FULFILLED,
                        PrescriptionStatus.COMPLETED
                )
        );
    }

    @Test
    void shouldRejectCompletedToCancelled() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.validate(
                        PrescriptionStatus.COMPLETED,
                        PrescriptionStatus.CANCELLED
                )
        );
    }

    @Test
    void shouldRejectCancelledToCompleted() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.validate(
                        PrescriptionStatus.CANCELLED,
                        PrescriptionStatus.COMPLETED
                )
        );
    }
}