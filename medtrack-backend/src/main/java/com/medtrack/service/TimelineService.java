package com.medtrack.service;

import com.medtrack.dto.TimelineItemResponse;
import com.medtrack.repository.VisitRepository;
import com.medtrack.repository.PrescriptionRepository;
import com.medtrack.repository.MedicalReportRepository;
import com.medtrack.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimelineService {

    private final VisitRepository visitRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final PatientRepository patientRepository;

    public TimelineService(VisitRepository visitRepository,
                           PrescriptionRepository prescriptionRepository,
                           MedicalReportRepository medicalReportRepository,
                           PatientRepository patientRepository) {
        this.visitRepository = visitRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicalReportRepository = medicalReportRepository;
        this.patientRepository = patientRepository;
    }

    public List<TimelineItemResponse> getPatientTimeline(Long patientId) {
        patientRepository.findByIdOrThrow(patientId);

        List<TimelineItemResponse> timelineItems = new ArrayList<>();

        visitRepository.findAllByPatientId(patientId).forEach(visit -> {
            timelineItems.add(new TimelineItemResponse(
                    visit.getId(), "VISIT", visit.getVisitDate(), "Medical Visit", visit.getNotes()
            ));
        });

        prescriptionRepository.findAllByPatientId(patientId).forEach(prescription -> {
            timelineItems.add(new TimelineItemResponse(
                    prescription.getId(), "PRESCRIPTION", prescription.getIssueDate(), "Prescription Issued", prescription.getNotes()
            ));
        });

        medicalReportRepository.findAllByPatientId(patientId).forEach(report -> {
            timelineItems.add(new TimelineItemResponse(
                    report.getId(),
                    "REPORT",
                    report.getReportDate(),
                    report.getReportType() != null ? report.getReportType().name() : "Medical Report",
                    report.getDescription()
            ));
        });

        return timelineItems.stream()
                .sorted(Comparator.comparing(TimelineItemResponse::getDate).reversed())
                .collect(Collectors.toList());
    }
}