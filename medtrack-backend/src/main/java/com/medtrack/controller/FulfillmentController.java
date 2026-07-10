package com.medtrack.controller;

import com.medtrack.dto.FulfillmentResponse;
import com.medtrack.dto.RejectFulfillmentRequest;
import com.medtrack.service.FulfillmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fulfillments")
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;

    public FulfillmentController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<FulfillmentResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(fulfillmentService.accept(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<FulfillmentResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectFulfillmentRequest request) {
        return ResponseEntity.ok(fulfillmentService.reject(id, request));
    }
    @PatchMapping("/{id}/preparing")
    public ResponseEntity<FulfillmentResponse> preparing(@PathVariable Long id) {
        return ResponseEntity.ok(fulfillmentService.preparing(id));
    }

    @PatchMapping("/{id}/ready")
    public ResponseEntity<FulfillmentResponse> ready(@PathVariable Long id) {
        return ResponseEntity.ok(fulfillmentService.ready(id));
    }

    @PatchMapping("/{id}/completed")
    public ResponseEntity<FulfillmentResponse> completed(@PathVariable Long id) {
        return ResponseEntity.ok(fulfillmentService.completed(id));
    }
}