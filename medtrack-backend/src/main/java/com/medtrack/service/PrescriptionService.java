package com.medtrack.service;
import com.medtrack.dto.*; import com.medtrack.entity.*; import com.medtrack.repository.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.util.List; import java.util.stream.Collectors;
@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository; private final PrescriptionItemRepository prescriptionItemRepository; private final PatientRepository patientRepository; private final DoctorRepository doctorRepository; private final VisitRepository visitRepository; private final MedicationRepository medicationRepository;
    public PrescriptionService(PrescriptionRepository pr, PrescriptionItemRepository pir, PatientRepository par, DoctorRepository dr, VisitRepository vr, MedicationRepository mr) {
        this.prescriptionRepository = pr; this.prescriptionItemRepository = pir; this.patientRepository = par; this.doctorRepository = dr; this.visitRepository = vr; this.medicationRepository = mr;
    }
    @Transactional public PrescriptionResponse createPrescription(PrescriptionRequest request) {
        Patient p = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        Doctor d = doctorRepository.findById(request.getDoctorId()).orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        Visit v = null; if (request.getVisitId() != null) v = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new IllegalArgumentException("Visit not found"));
        Prescription prescription = new Prescription(); prescription.setPatient(p); prescription.setDoctor(d); prescription.setVisit(v); prescription.setStatus(request.getStatus()); prescription.setIssueDate(request.getIssueDate()); prescription.setNotes(request.getNotes());
        Prescription saved = prescriptionRepository.save(prescription);
        if (request.getItems() != null) {
            for (PrescriptionItemRequest ir : request.getItems()) {
                Medication m = medicationRepository.findById(ir.getMedicationId()).orElseThrow(() -> new IllegalArgumentException("Medication not found"));
                PrescriptionItem item = new PrescriptionItem(); item.setPrescription(saved); item.setMedication(m); item.setDosage(ir.getDosage()); item.setFrequency(ir.getFrequency()); item.setDurationDays(ir.getDurationDays()); item.setQuantity(ir.getQuantity()); item.setInstructions(ir.getInstructions());
                prescriptionItemRepository.save(item);
            }
        }
        return getPrescriptionById(saved.getId());
    }
    @Transactional(readOnly = true) public PrescriptionResponse getPrescriptionById(Long id) { Prescription p = prescriptionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Prescription not found")); return toResponse(p, prescriptionItemRepository.findByPrescriptionId(id)); }
    @Transactional(readOnly = true) public List<PrescriptionResponse> getPrescriptionsByPatient(Long patientId) { return prescriptionRepository.findByPatientId(patientId).stream().map(p -> toResponse(p, prescriptionItemRepository.findByPrescriptionId(p.getId()))).collect(Collectors.toList()); }
    private PrescriptionResponse toResponse(Prescription p, List<PrescriptionItem> items) {
        PrescriptionResponse r = new PrescriptionResponse(); r.setId(p.getId()); r.setPatientId(p.getPatient().getId()); r.setDoctorId(p.getDoctor().getId()); if (p.getVisit() != null) r.setVisitId(p.getVisit().getId());
        r.setStatus(p.getStatus()); r.setIssueDate(p.getIssueDate()); r.setNotes(p.getNotes()); r.setCreatedAt(p.getCreatedAt()); r.setUpdatedAt(p.getUpdatedAt());
        if (items != null) r.setItems(items.stream().map(this::toItemResponse).collect(Collectors.toList()));
        return r;
    }
    private PrescriptionItemResponse toItemResponse(PrescriptionItem i) {
        PrescriptionItemResponse r = new PrescriptionItemResponse(); r.setId(i.getId()); r.setMedicationId(i.getMedication().getId()); r.setMedicationName(i.getMedication().getName()); r.setDosage(i.getDosage()); r.setFrequency(i.getFrequency()); r.setDurationDays(i.getDurationDays()); r.setQuantity(i.getQuantity()); r.setInstructions(i.getInstructions()); r.setCreatedAt(i.getCreatedAt()); r.setUpdatedAt(i.getUpdatedAt());
        return r;
    }
}
