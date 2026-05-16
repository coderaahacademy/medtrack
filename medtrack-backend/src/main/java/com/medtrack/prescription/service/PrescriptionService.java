package com.medtrack.prescription.service;

import com.medtrack.doctor.entity.Doctor;
import com.medtrack.doctor.repository.DoctorRepository;
import com.medtrack.medication.entity.Medication;
import com.medtrack.medication.repository.MedicationRepository;
import com.medtrack.patient.entity.Patient;
import com.medtrack.patient.repository.PatientRepository;
import com.medtrack.prescription.dto.*;
import com.medtrack.prescription.entity.Prescription;
import com.medtrack.prescription.entity.PrescriptionItem;
import com.medtrack.prescription.repository.PrescriptionRepository;
import com.medtrack.visit.entity.Visit;
import com.medtrack.visit.repository.VisitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;
    private final MedicationRepository medicationRepository;
    public PrescriptionService(PrescriptionRepository prescriptionRepository, PatientRepository patientRepository, DoctorRepository doctorRepository,
                               VisitRepository visitRepository, MedicationRepository medicationRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.visitRepository = visitRepository;
        this.medicationRepository = medicationRepository;
    }
    @Transactional
    public PrescriptionResponse createPrescription(PrescriptionRequest request) {
        Long patientId = request.getPatientId();
        Long doctorId = request.getDoctorId();
        Long visitId = request.getVisitId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with ID: " + patientId));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: " + doctorId));
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found with ID: " + visitId));
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setVisit(visit);
        prescription.setStatus(request.getStatus());
        prescription.setIssueDate(request.getIssueDate());
        prescription.setNotes(request.getNotes());
        request.getItems().forEach(itemRequest -> {
            Long medicationId = itemRequest.getMedicationId();
            Medication medication = medicationRepository.findById(medicationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found with ID: " + medicationId));
            PrescriptionItem item = new PrescriptionItem();
            item.setMedication(medication);
            item.setDosage(itemRequest.getDosage());
            item.setFrequency(itemRequest.getFrequency());
            item.setDurationDays(itemRequest.getDurationDays());
            item.setQuantity(itemRequest.getQuantity());
            item.setInstructions(itemRequest.getInstructions());
            prescription.addItem(item);
        });
        Prescription savedPrescription = prescriptionRepository.saveAndFlush(prescription);
        return toResponse(savedPrescription);
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        PrescriptionResponse response = new PrescriptionResponse();
        response.setPatientId(prescription.getPatient().getId());
        response.setDoctorId(prescription.getDoctor().getId());
        response.setVisitId(prescription.getVisit().getId());
        response.setStatus(prescription.getStatus());
        response.setIssueDate(prescription.getIssueDate());
        response.setNotes(prescription.getNotes());
        response.setCreatedAt(prescription.getCreatedAt());
        response.setUpdatedAt(prescription.getUpdatedAt());
        List<PrescriptionItemResponse> itemResponses = prescription.getItems()
                .stream().map(this::toItemResponse).collect(Collectors.toList());
        response.setItems(itemResponses);
        response.setId(prescription.getId());
        return response;
    }

    private PrescriptionItemResponse toItemResponse(PrescriptionItem prescriptionItem) {
        PrescriptionItemResponse response = new PrescriptionItemResponse();
        response.setDosage(prescriptionItem.getDosage());
        response.setFrequency(prescriptionItem.getFrequency());
        response.setDurationDays(prescriptionItem.getDurationDays());
        response.setQuantity(prescriptionItem.getQuantity());
        response.setInstructions(prescriptionItem.getInstructions());
        response.setCreatedAt(prescriptionItem.getCreatedAt());
        response.setUpdatedAt(prescriptionItem.getUpdatedAt());
        response.setPrescriptionId(prescriptionItem.getPrescription().getId());
        response.setMedicationId(prescriptionItem.getMedication().getId());
        response.setId(prescriptionItem.getId());
        return response;
    }
}
