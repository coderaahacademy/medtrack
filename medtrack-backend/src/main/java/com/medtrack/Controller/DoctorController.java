package com.medtrack.controller;
import com.medtrack.dto.*; import com.medtrack.service.DoctorService; import jakarta.validation.Valid; import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable; import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService doctorService;
    public DoctorController(DoctorService doctorService) { this.doctorService = doctorService; }
    @PostMapping public ResponseEntity<DoctorResponse> create(@Valid @RequestBody CreateDoctorRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(request)); }
    @GetMapping public ResponseEntity<Page<DoctorResponse>> getAll(Pageable pageable) { return ResponseEntity.ok(doctorService.getAllDoctors(pageable)); }
    @GetMapping("/{id}") public ResponseEntity<DoctorResponse> getById(@PathVariable Long id) { return ResponseEntity.ok(doctorService.getDoctorById(id)); }
    @PutMapping("/{id}") public ResponseEntity<DoctorResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateDoctorRequest request) { return ResponseEntity.ok(doctorService.updateDoctor(id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<String> delete(@PathVariable Long id) { return ResponseEntity.ok(doctorService.deleteDoctor(id)); }
}
