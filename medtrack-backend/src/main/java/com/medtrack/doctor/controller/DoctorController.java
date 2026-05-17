package com.medtrack.doctor.controller;

import com.medtrack.doctor.dto.CreateDoctorRequest;
import com.medtrack.doctor.dto.DoctorResponse;
import com.medtrack.doctor.dto.UpdateDoctorRequest;
import jakarta.validation.Valid;
import com.medtrack.doctor.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }
    @PostMapping
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody CreateDoctorRequest request){
        DoctorResponse body= doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public List<DoctorResponse> getAll(){
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getById(@PathVariable Long id){
        DoctorResponse body= doctorService.getDoctorById(id);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id){
        doctorService.deleteDoctor(id);
        return "Successfully deleted";
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> update(@Valid @PathVariable Long id, @RequestBody UpdateDoctorRequest request){
        DoctorResponse body= doctorService.updateDoctor(id,request);
        return ResponseEntity.ok(body);
    }
}
