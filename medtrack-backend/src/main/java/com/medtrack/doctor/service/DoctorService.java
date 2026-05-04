package com.medtrack.doctor.service;

import com.medtrack.doctor.api.CreateDoctorRequest;
import com.medtrack.doctor.api.UpdateDoctorRequest;
import com.medtrack.doctor.domain.Doctor;
import com.medtrack.doctor.repository.DoctorRepository;
import com.medtrack.user.domain.User;
import com.medtrack.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository=userRepository;
    }

    @Transactional
    public void createDoctor(CreateDoctorRequest request){
        Optional<User> userOptional = userRepository.findById(request.getUserId());
        if(userOptional.isEmpty()){
            throw new IllegalArgumentException("User not found with ID: "+request.getUserId());
        }
        User user=userOptional.get();
        Doctor doctor= new Doctor();
        doctor.setFullname(request.getFullname());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setUser(user);
        doctorRepository.save(doctor);
    }

    public List<Doctor> getAllDoctors(){
        return doctorRepository.findAll();
    }

    public Doctor getDoctorById(Long id){
        Optional<Doctor> doctorOptional=doctorRepository.findById(id);
        if(!doctorRepository.existsById(id)){
            throw new IllegalArgumentException("Doctor not found with id: "+id);
        }
        return doctorOptional.get();
    }

    @Transactional
    public void deleteDoctor(Long id){
        if(!doctorRepository.existsById(id)){
            throw new IllegalArgumentException("Cannot delete. Doctor not found with id: "+id);
        }
        doctorRepository.deleteById(id);
    }

    @Transactional
    public void updateDoctor(Long id, UpdateDoctorRequest request){
        Optional<Doctor> doctorOptional=doctorRepository.findById(id);
        if (doctorOptional.isEmpty()){
            throw new IllegalArgumentException("Doctor not found with id "+ id);
        }
        Doctor doctor=doctorOptional.get();
        doctor.setFullname(request.getFullname());
        doctor.setSpecialization(request.getSpecialization());
        doctorRepository.save(doctor);
    }


}
