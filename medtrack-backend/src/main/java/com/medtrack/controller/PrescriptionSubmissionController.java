package com.medtrack.controller;

import com.medtrack.dto.SendToPharmacyRequest;
import com.medtrack.dto.SendToPharmacyResponse;
import com.medtrack.service.PrescriptionSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescriptions")
@Tag(
        name = "Prescription Submission",
        description = "Operations for sending prescriptions to pharmacies"
)
public class PrescriptionSubmissionController {

    private final PrescriptionSubmissionService submissionService;

    public PrescriptionSubmissionController(
            PrescriptionSubmissionService submissionService
    ) {
        this.submissionService = submissionService;
    }

    @Operation(
            summary = "Send a prescription to a pharmacy",
            description = """
                    Sends an ISSUED prescription to the selected pharmacy,
                    creates a PENDING fulfillment request and changes the
                    prescription status to SENT_TO_PHARMACY.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Prescription successfully sent"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Prescription or pharmacy not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid prescription state or duplicate submission"
            )
    })
    @PostMapping("/{prescriptionId}/send-to-pharmacy")
    public ResponseEntity<SendToPharmacyResponse> sendToPharmacy(
            @PathVariable Long prescriptionId,
            @Valid @RequestBody SendToPharmacyRequest request
    ) {
        SendToPharmacyResponse response =
                submissionService.sendToPharmacy(
                        prescriptionId,
                        request.getPharmacyId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
