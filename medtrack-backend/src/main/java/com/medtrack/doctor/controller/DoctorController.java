package com.medtrack.doctor.controller;

import com.medtrack.doctor.dto.*;
import com.medtrack.doctor.service.DoctorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(@RequestBody CreateDoctorRequest request){
        DoctorResponse body = doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<Page<DoctorResponse>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable  pageable = PageRequest.of(page, size);
        Page<DoctorResponse> body = doctorService.getAllDoctors(pageable);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable long id){
        DoctorResponse body = doctorService.getDoctorById(id);
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(@PathVariable long id, @RequestBody UpdateDoctorRequest request){
        DoctorResponse body = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDoctor(@PathVariable long id){
        String successMessage = doctorService.deleteDoctor(id);
        return ResponseEntity.ok(successMessage);
    }
}
