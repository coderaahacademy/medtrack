package com.medtrack.doctor.api;

import com.medtrack.doctor.domain.Doctor;
import com.medtrack.doctor.service.DoctorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }
    @PostMapping("/create")
    public String create(@RequestBody CreateDoctorRequest request){
        doctorService.createDoctor(request);
        return "Successfully created";
    }

    @GetMapping
    public List<Doctor> getAll(){
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public Doctor getById(@PathVariable Long id){
        return doctorService.getDoctorById(id);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id){
        doctorService.deleteDoctor(id);
        return "Successfully deleted";
    }

    @PutMapping("/{id}")
    public String  update(@PathVariable Long id, @RequestBody UpdateDoctorRequest request){
        doctorService.updateDoctor(id,request);
        return "Successfully deleted";
    }
}
