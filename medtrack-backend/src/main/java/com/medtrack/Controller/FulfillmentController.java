package com.medtrack.controller;
import com.medtrack.entity.Fulfillment; import com.medtrack.service.FulfillmentService; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/fulfillments")
public class FulfillmentController {
    private final FulfillmentService fulfillmentService;
    public FulfillmentController(FulfillmentService fulfillmentService) { this.fulfillmentService = fulfillmentService; }
    @GetMapping public ResponseEntity<List<Fulfillment>> getAllFulfillments() { return ResponseEntity.ok(fulfillmentService.getAllFulfillments()); }
    @PostMapping public ResponseEntity<Fulfillment> createFulfillment(@RequestBody Fulfillment fulfillment) { return ResponseEntity.ok(fulfillmentService.createFulfillment(fulfillment)); }
}
