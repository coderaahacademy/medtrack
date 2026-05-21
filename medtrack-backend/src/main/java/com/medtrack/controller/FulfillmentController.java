package com.medtrack.controller;

import com.medtrack.entity.Fulfillment;
import com.medtrack.service.FulfillmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fulfillments")
public class FulfillmentController {

    private final FulfillmentService service;

    public FulfillmentController(FulfillmentService service) {
        this.service = service;
    }

    @PostMapping
    public Fulfillment createFulfillment(@RequestBody Fulfillment fulfillment) {
        return service.saveFulfillment(fulfillment);
    }
}