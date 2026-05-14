package com.medtrack.patient.service;

import com.medtrack.entity.Patient;
import com.medtrack.entity.Doctor;
import com.medtrack.patient.repository.PatientRepository;
import com.medtrack.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository,
                          DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public void assignFamilyDoctor(Long patientId, Long doctorId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        patient.setFamilyDoctor(doctor);

        patientRepository.save(patient);
    }
}