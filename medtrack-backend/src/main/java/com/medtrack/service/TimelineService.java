package com.medtrack.service;

import com.medtrack.dto.TimelineItemDTO;
import com.medtrack.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TimelineService {

    private final PatientRepository patientRepository;

    public TimelineService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<TimelineItemDTO> getPatientTimeline(Long patientId) {

        if (!patientRepository.existsById(patientId)) {
            throw new IllegalArgumentException("Patient not found with ID: " + patientId);
        }


        List<TimelineItemDTO> timeline = new ArrayList<>();

        return timeline;
    }
}