package com.medtrack.controller;

import com.medtrack.dto.CreateDoctorAvailabilityRequest;
import com.medtrack.dto.DoctorAvailabilityResponse;
import com.medtrack.service.DoctorAvailabilityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctor-availability")
@SecurityRequirement(name = "bearerAuth")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public DoctorAvailabilityController(
            DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @PreAuthorize("@authz.canManageDoctorAvailability(#request.doctorId)")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorAvailabilityResponse create(
            @Valid @RequestBody CreateDoctorAvailabilityRequest request) {

        return availabilityService.create(request);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("@authz.canReadDoctorAvailability(#doctorId)")
    public List<DoctorAvailabilityResponse> getByDoctorId(
            @PathVariable Long doctorId) {

        return availabilityService.getByDoctorId(doctorId);
    }

    @GetMapping("/doctor/{doctorId}/range")
    @PreAuthorize("@authz.canReadDoctorAvailability(#doctorId)")
    public List<DoctorAvailabilityResponse> getByDoctorIdAndDateRange(
            @PathVariable Long doctorId,
            @RequestParam LocalDateTime startAt,
            @RequestParam LocalDateTime endAt) {

        return availabilityService.getByDoctorIdAndDateRange(
                doctorId,
                startAt,
                endAt
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authz.canManageDoctorAvailabilityById(#id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        availabilityService.delete(id);
    }
}