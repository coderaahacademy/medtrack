package com.medtrack.service;

import com.medtrack.dto.PatientTimelineResponse;
import com.medtrack.dto.TimelineItemResponse;
import com.medtrack.repository.MedicalReportRepository;
import com.medtrack.repository.PrescriptionRepository;
import com.medtrack.repository.VisitRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TimelineService {

    private final VisitRepository visitRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalReportRepository medicalReportRepository;

    public TimelineService(VisitRepository visitRepository,
                           PrescriptionRepository prescriptionRepository,
                           MedicalReportRepository medicalReportRepository) {
        this.visitRepository = visitRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicalReportRepository = medicalReportRepository;
    }

    public PatientTimelineResponse getPatientTimeline(Long patientId) {
        List<TimelineItemResponse> timelineItems = new ArrayList<>();

        visitRepository.findAllByPatientId(patientId).forEach(visit -> {
            timelineItems.add(new TimelineItemResponse(
                    "VISIT",
                    visit.getVisitDate(),
                    "Visit with Doctor",
                    "Symptoms: " + visit.getSymptoms() + " | Diagnosis: " + visit.getDiagnosis()
            ));
        });

        prescriptionRepository.findAllByPatientId(patientId).forEach(prescription -> {
            timelineItems.add(new TimelineItemResponse(
                    "PRESCRIPTION",
                    prescription.getIssueDate(),
                    "Prescription Issued",
                    "Status: " + prescription.getStatus() + " | Notes: " + prescription.getNotes()
            ));
        });

        medicalReportRepository.findByPatientId(patientId).forEach(report -> {
            timelineItems.add(new TimelineItemResponse(
                    "MEDICAL_REPORT",
                    report.getReportDate(),
                    report.getTitle(),
                    report.getDescription()
            ));
        });

        timelineItems.sort(Comparator.comparing(TimelineItemResponse::getDate).reversed());

        return new PatientTimelineResponse(patientId, timelineItems);
    }
}