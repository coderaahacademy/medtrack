package com.medtrack.service;

import com.medtrack.dto.CreateDoctorAvailabilityRequest;
import com.medtrack.dto.DoctorAvailabilityResponse;
import com.medtrack.entity.Doctor;
import com.medtrack.entity.DoctorAvailability;
import com.medtrack.repository.DoctorAvailabilityRepository;
import com.medtrack.repository.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;

    public DoctorAvailabilityService(
            DoctorAvailabilityRepository availabilityRepository,
            DoctorRepository doctorRepository) {
        this.availabilityRepository = availabilityRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public DoctorAvailabilityResponse create(CreateDoctorAvailabilityRequest request) {

        Doctor doctor = doctorRepository.findWithLockById(request.getDoctorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Doctor not found"
                ));

        if (!doctor.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor must be active"
            );
        }

        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startAt must be before endAt"
            );
        }

        List<DoctorAvailability> overlapping =
                availabilityRepository.findByDoctorIdAndStartAtLessThanAndEndAtGreaterThan(
                        request.getDoctorId(),
                        request.getEndAt(),
                        request.getStartAt()
                );

        if (!overlapping.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Doctor availability overlaps with an existing availability"
            );
        }

        DoctorAvailability availability = new DoctorAvailability();
        availability.setDoctor(doctor);
        availability.setStartAt(request.getStartAt());
        availability.setEndAt(request.getEndAt());

        return toResponse(availabilityRepository.saveAndFlush(availability));
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> getByDoctorId(Long doctorId) {

        Doctor doctor = doctorRepository.findByIdOrThrow(doctorId);

        if (!doctor.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor must be active"
            );
        }

        return availabilityRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> getByDoctorIdAndDateRange(
            Long doctorId,
            LocalDateTime startAt,
            LocalDateTime endAt) {

        Doctor doctor = doctorRepository.findByIdOrThrow(doctorId);

        if (!doctor.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor must be active"
            );
        }

        if (!startAt.isBefore(endAt)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startAt must be before endAt"
            );
        }

        return availabilityRepository
                .findByDoctorIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                        doctorId,
                        endAt,
                        startAt
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {

        DoctorAvailability availability =
                availabilityRepository.findByIdOrThrow(id);

        availabilityRepository.delete(availability);
    }

    private DoctorAvailabilityResponse toResponse(
            DoctorAvailability availability) {

        DoctorAvailabilityResponse response =
                new DoctorAvailabilityResponse();

        response.setId(availability.getId());
        response.setDoctorId(availability.getDoctor().getId());
        response.setStartAt(availability.getStartAt());
        response.setEndAt(availability.getEndAt());

        return response;
    }
}