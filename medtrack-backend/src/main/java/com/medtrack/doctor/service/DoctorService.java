package com.medtrack.doctor.service;

import com.medtrack.doctor.dto.CreateDoctorRequest;
import com.medtrack.doctor.dto.DoctorResponse;
import com.medtrack.doctor.dto.UpdateDoctorRequest;
import com.medtrack.doctor.entity.Doctor;
import com.medtrack.repository.DoctorRepository;
import com.medtrack.entity.User;
import com.medtrack.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository=userRepository;
    }

    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request){
        User user=userRepository.findById(request.getUserId()).orElseThrow(()->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found with ID: "+request.getUserId()));
        Doctor doctor=new Doctor();
        doctor.setFullName(request.getFullName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPhone(request.getPhone());
        doctor.setActive(true);
        doctor.setUser(user);
        Doctor savedDoctor=doctorRepository.save(doctor);
        return DoctorResponse.mapToResponse(savedDoctor);
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(DoctorResponse::mapToResponse)
                .collect(Collectors.toList());
    }

    public DoctorResponse getDoctorById(Long id){
        Doctor doctor =doctorRepository.findById(id).orElseThrow(()-> new
               ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found with id: "+id));
        return DoctorResponse.mapToResponse(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id){
        if(!doctorRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found with id: "+id);
        }
        doctorRepository.deleteById(id);
    }

    @Transactional
    public DoctorResponse updateDoctor(Long id, UpdateDoctorRequest request){
        Doctor doctor=doctorRepository.findById(id).orElseThrow(()-> new
                ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found with id: "+id));
        doctor.setFullName(request.getFullName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPhone(request.getPhone());
        doctor.setActive(request.isActive());
        Doctor updatedDoctor=doctorRepository.save(doctor);
        return DoctorResponse.mapToResponse(updatedDoctor);
    }


}
