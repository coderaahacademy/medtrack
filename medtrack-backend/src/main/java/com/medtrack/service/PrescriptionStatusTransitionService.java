package com.medtrack.service;

import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidStatusTransitionException;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class PrescriptionStatusTransitionService {

    private final Map<PrescriptionStatus, Set<PrescriptionStatus>> allowedTransitions =
            new EnumMap<>(PrescriptionStatus.class);

    public PrescriptionStatusTransitionService() {

        allowedTransitions.put(
                PrescriptionStatus.DRAFT,
                EnumSet.noneOf(PrescriptionStatus.class)
        );


        allowedTransitions.put(
                PrescriptionStatus.ISSUED,
                EnumSet.of(
                        PrescriptionStatus.SENT_TO_PHARMACY,
                        PrescriptionStatus.CANCELLED
                )
        );

        allowedTransitions.put(
                PrescriptionStatus.SENT_TO_PHARMACY,
                EnumSet.of(
                        PrescriptionStatus.PARTIALLY_FULFILLED,
                        PrescriptionStatus.COMPLETED,
                        PrescriptionStatus.CANCELLED
                )
        );

        allowedTransitions.put(
                PrescriptionStatus.PARTIALLY_FULFILLED,
                EnumSet.of(
                        PrescriptionStatus.COMPLETED
                )
        );

        allowedTransitions.put(
                PrescriptionStatus.COMPLETED,
                EnumSet.noneOf(PrescriptionStatus.class)
        );

        allowedTransitions.put(
                PrescriptionStatus.CANCELLED,
                EnumSet.noneOf(PrescriptionStatus.class)
        );
    }

    public void validate(
            PrescriptionStatus currentStatus,
            PrescriptionStatus newStatus
    ) {
        if (currentStatus == null || newStatus == null) {
            throw new InvalidStatusTransitionException(
                    "Prescription status transition cannot use null status"
            );
        }

        if (currentStatus == newStatus) {
            throw new InvalidStatusTransitionException(
                    "Prescription cannot transition to the same status: "
                            + currentStatus
            );
        }

        Set<PrescriptionStatus> allowed =
                allowedTransitions.get(currentStatus);

        if (allowed == null || !allowed.contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Invalid prescription status transition: "
                            + currentStatus + " -> " + newStatus
            );
        }
    }
}