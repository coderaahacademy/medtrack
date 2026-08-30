package com.medtrack.controller;

import com.medtrack.dto.CreateDoctorAvailabilityRequest;
import com.medtrack.dto.DoctorAvailabilityResponse;
import com.medtrack.service.DoctorAvailabilityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor-availability")
@SecurityRequirement(name = "bearerAuth")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public DoctorAvailabilityController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorAvailabilityResponse create(
            @RequestBody CreateDoctorAvailabilityRequest request) {
        return availabilityService.create(request);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<DoctorAvailabilityResponse> getByDoctorId(
            @PathVariable Long doctorId) {
        return availabilityService.getByDoctorId(doctorId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        availabilityService.delete(id);
    }
}