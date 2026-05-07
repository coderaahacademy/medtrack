package com.medtrack.doctor.service;

import com.medtrack.doctor.api.CreateDoctorRequest;
import com.medtrack.doctor.api.DoctorResponse;
import com.medtrack.doctor.api.UpdateDoctorRequest;
import com.medtrack.doctor.domain.Doctor;
import com.medtrack.doctor.repository.DoctorRepository;
import com.medtrack.user.domain.User;
import com.medtrack.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
        doctor.setUser(user);
        Doctor savedDoctor=doctorRepository.save(doctor);
        return DoctorResponse.mapToResponse(savedDoctor);
    }

    public List<Doctor> getAllDoctors(){
        return doctorRepository.findAll();
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
        if(request.getFullName()!= null){
            doctor.setFullName(request.getFullName());
        }
        if(request.getSpecialization()!=null){
            doctor.setSpecialization(request.getSpecialization());
        }
        Doctor updatedDoctor=doctorRepository.save(doctor);
        return DoctorResponse.mapToResponse(updatedDoctor);
    }


}
