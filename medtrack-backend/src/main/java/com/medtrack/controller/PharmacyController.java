package com.medtrack.controller;

import com.medtrack.dto.CreatePharmacyRequest;
import com.medtrack.dto.FulfillmentSummaryResponse;
import com.medtrack.dto.PharmacyRequest;
import com.medtrack.dto.PharmacyResponse;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.service.FulfillmentQueryService;
import com.medtrack.service.PharmacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharmacies")
public class PharmacyController {
    private final PharmacyService pharmacyService;
    private final FulfillmentQueryService fulfillmentQueryService;

    public PharmacyController(PharmacyService pharmacyService,
                              FulfillmentQueryService fulfillmentQueryService) {
        this.pharmacyService = pharmacyService;
        this.fulfillmentQueryService = fulfillmentQueryService;
    }

    @PostMapping
    @PreAuthorize("@authz.canCreatePharmacy(#request.userId)")
    public ResponseEntity<PharmacyResponse> create(@Valid @RequestBody CreatePharmacyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pharmacyService.create(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PharmacyResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(pharmacyService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PharmacyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacyService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authz.canModifyPharmacy(#id)")
    public ResponseEntity<PharmacyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PharmacyRequest request) {

        return ResponseEntity.ok(pharmacyService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PharmacyResponse> deactivate(@PathVariable Long id) {

        return ResponseEntity.ok(pharmacyService.deactivate(id));
    }

    @GetMapping("/{pharmacyId}/fulfillments")
    @PreAuthorize("@authz.canReadPharmacyQueue(#pharmacyId)")
    @Operation(summary = "Get the fulfillment work queue of a pharmacy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Queue page returned"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Caller may only read its own pharmacy queue"),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found")
    })
    public ResponseEntity<Page<FulfillmentSummaryResponse>> getFulfillmentQueue(
            @PathVariable Long pharmacyId,
            @RequestParam(required = false) FulfillmentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                fulfillmentQueryService.getPharmacyQueue(pharmacyId, status, pageable));
    }
}