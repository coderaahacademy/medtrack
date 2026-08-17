package com.medtrack.controller;

import com.medtrack.dto.CancelPrescriptionRequest;
import com.medtrack.dto.PrescriptionRequest;
import com.medtrack.dto.PrescriptionResponse;
import com.medtrack.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.medtrack.dto.PrescriptionStatusUpdateRequest;


@RestController
@RequestMapping("/api/prescriptions")
@Tag(name = "Prescriptions", description = "Prescription management endpoints")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    @Operation(summary = "Create a new prescription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Prescription successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input, duplicate medications, or empty medication list"),
            @ApiResponse(responseCode = "404", description = "Referenced patient, doctor, or medication not found")
    })
    public ResponseEntity<PrescriptionResponse> create(@Valid @RequestBody PrescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<PrescriptionResponse>> getAllPrescriptions(Pageable pageable){
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions(pageable));

    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(prescriptionService.getById(id));
    }
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update prescription status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prescription status successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Prescription not found"),
            @ApiResponse(responseCode = "409", description = "Invalid prescription status transition")
    })
    public ResponseEntity<PrescriptionResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionStatusUpdateRequest request) {

        return ResponseEntity.ok(
                prescriptionService.updateStatus(id, request.getStatus())
        );
    }
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a prescription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prescription successfully cancelled"),
            @ApiResponse(responseCode = "400", description = "Invalid request or missing cancellation reason"),
            @ApiResponse(responseCode = "404", description = "Prescription not found"),
            @ApiResponse(responseCode = "409", description = "Prescription cannot be cancelled from current state")
    })
    public ResponseEntity<PrescriptionResponse> cancelPrescription(
            @PathVariable Long id,
            @Valid @RequestBody CancelPrescriptionRequest request) {
        return ResponseEntity.ok(
                prescriptionService.cancelPrescription(
                        id,
                        request.getReason()
                )
        );
    }

}
