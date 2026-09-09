package com.medtrack.controller;

import com.medtrack.dto.FulfillmentDetailResponse;
import com.medtrack.dto.FulfillmentResponse;
import com.medtrack.dto.RejectFulfillmentRequest;
import com.medtrack.service.FulfillmentQueryService;
import com.medtrack.service.FulfillmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.medtrack.dto.CompleteFulfillmentRequest;

@RestController
@RequestMapping("/api/fulfillments")
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;
    private final FulfillmentQueryService fulfillmentQueryService;

    public FulfillmentController(FulfillmentService fulfillmentService,
                                 FulfillmentQueryService fulfillmentQueryService) {
        this.fulfillmentService = fulfillmentService;
        this.fulfillmentQueryService = fulfillmentQueryService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authz.canReadFulfillment(#id)")
    @Operation(summary = "Get full dispensing details of a fulfillment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fulfillment details returned"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Caller does not own this fulfillment"),
            @ApiResponse(responseCode = "404", description = "Fulfillment not found")
    })
    public ResponseEntity<FulfillmentDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fulfillmentQueryService.getFulfillmentDetail(id));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("@authz.canModifyFulfillment(#id)")
    public ResponseEntity<FulfillmentResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(fulfillmentService.accept(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("@authz.canModifyFulfillment(#id)")
    public ResponseEntity<FulfillmentResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectFulfillmentRequest request) {
        return ResponseEntity.ok(fulfillmentService.reject(id, request));
    }

    @PatchMapping("/{id}/preparing")
    @PreAuthorize("@authz.canModifyFulfillment(#id)")
    public ResponseEntity<FulfillmentResponse> preparing(@PathVariable Long id) {
        return ResponseEntity.ok(fulfillmentService.preparing(id));
    }

    @PatchMapping("/{id}/ready")
    @PreAuthorize("@authz.canModifyFulfillment(#id)")
    public ResponseEntity<FulfillmentResponse> ready(@PathVariable Long id) {
        return ResponseEntity.ok(fulfillmentService.ready(id));
    }

    @PatchMapping("/{id}/completed")
    @PreAuthorize("@authz.canModifyFulfillment(#id)")
    public ResponseEntity<FulfillmentResponse> completed(
            @PathVariable Long id,
            @Valid @RequestBody CompleteFulfillmentRequest request) {
        return ResponseEntity.ok(fulfillmentService.completed(id, request));
    }
}