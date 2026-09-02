package com.medtrack.service;

import com.medtrack.dto.CreateDoctorAvailabilityRequest;
import com.medtrack.dto.DoctorAvailabilityResponse;
import com.medtrack.entity.Doctor;
import com.medtrack.entity.DoctorAvailability;
import com.medtrack.repository.DoctorAvailabilityRepository;
import com.medtrack.repository.DoctorRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceTest {

    @Mock
    private DoctorAvailabilityRepository availabilityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorAvailabilityService availabilityService;

    @Test
    void create_shouldCreateAvailabilitySuccessfully() {

        // Arrange
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endAt = startAt.plusHours(2);

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(1L);
        request.setStartAt(startAt);
        request.setEndAt(endAt);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setActive(true);

        DoctorAvailability savedAvailability =
                new DoctorAvailability();

        savedAvailability.setId(100L);
        savedAvailability.setDoctor(doctor);
        savedAvailability.setStartAt(startAt);
        savedAvailability.setEndAt(endAt);

        when(doctorRepository.findWithLockById(1L))
                .thenReturn(Optional.of(doctor));

        when(availabilityRepository
                .findByDoctorIdAndStartAtLessThanAndEndAtGreaterThan(
                        1L,
                        endAt,
                        startAt))
                .thenReturn(List.of());

        when(availabilityRepository.saveAndFlush(
                any(DoctorAvailability.class)))
                .thenReturn(savedAvailability);

        // Act
        DoctorAvailabilityResponse response =
                availabilityService.create(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(100L, response.getId());
        Assertions.assertEquals(1L, response.getDoctorId());
        Assertions.assertEquals(startAt, response.getStartAt());
        Assertions.assertEquals(endAt, response.getEndAt());

        verify(doctorRepository).findWithLockById(1L);

        verify(availabilityRepository)
                .findByDoctorIdAndStartAtLessThanAndEndAtGreaterThan(
                        1L,
                        endAt,
                        startAt);

        verify(availabilityRepository)
                .saveAndFlush(any(DoctorAvailability.class));
    }

    @Test
    void create_shouldRejectInactiveDoctor() {

        // Arrange
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endAt = startAt.plusHours(2);

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(1L);
        request.setStartAt(startAt);
        request.setEndAt(endAt);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setActive(false);

        when(doctorRepository.findWithLockById(1L))
                .thenReturn(Optional.of(doctor));

        // Act & Assert
        ResponseStatusException exception =
                Assertions.assertThrows(
                        ResponseStatusException.class,
                        () -> availabilityService.create(request)
                );

        Assertions.assertEquals(
                400,
                exception.getStatusCode().value()
        );

        Assertions.assertEquals(
                "Doctor must be active",
                exception.getReason()
        );

        verify(doctorRepository).findWithLockById(1L);
    }

    @Test
    void create_shouldRejectInvalidTimeRange() {

        // Arrange
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endAt = startAt.minusHours(1);

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(1L);
        request.setStartAt(startAt);
        request.setEndAt(endAt);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setActive(true);

        when(doctorRepository.findWithLockById(1L))
                .thenReturn(Optional.of(doctor));

        // Act & Assert
        ResponseStatusException exception =
                Assertions.assertThrows(
                        ResponseStatusException.class,
                        () -> availabilityService.create(request)
                );

        Assertions.assertEquals(
                400,
                exception.getStatusCode().value()
        );

        Assertions.assertEquals(
                "startAt must be before endAt",
                exception.getReason()
        );
    }

    @Test
    void create_shouldRejectOverlappingAvailability() {

        // Arrange
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endAt = startAt.plusHours(2);

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(1L);
        request.setStartAt(startAt);
        request.setEndAt(endAt);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setActive(true);

        DoctorAvailability existingAvailability =
                new DoctorAvailability();

        existingAvailability.setId(99L);
        existingAvailability.setDoctor(doctor);
        existingAvailability.setStartAt(startAt.minusHours(1));
        existingAvailability.setEndAt(startAt.plusHours(1));

        when(doctorRepository.findWithLockById(1L))
                .thenReturn(Optional.of(doctor));

        when(availabilityRepository
                .findByDoctorIdAndStartAtLessThanAndEndAtGreaterThan(
                        1L,
                        endAt,
                        startAt))
                .thenReturn(List.of(existingAvailability));

        // Act & Assert
        ResponseStatusException exception =
                Assertions.assertThrows(
                        ResponseStatusException.class,
                        () -> availabilityService.create(request)
                );

        Assertions.assertEquals(
                409,
                exception.getStatusCode().value()
        );

        Assertions.assertEquals(
                "Doctor availability overlaps with an existing availability",
                exception.getReason()
        );
    }

    @Test
    void create_shouldAllowAdjacentAvailabilityWindows() {

        // Arrange
        LocalDateTime existingStart =
                LocalDateTime.now().plusDays(1);

        LocalDateTime existingEnd =
                existingStart.plusHours(2);

        LocalDateTime newStart =
                existingEnd;

        LocalDateTime newEnd =
                newStart.plusHours(2);

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(1L);
        request.setStartAt(newStart);
        request.setEndAt(newEnd);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setActive(true);

        DoctorAvailability savedAvailability =
                new DoctorAvailability();

        savedAvailability.setId(101L);
        savedAvailability.setDoctor(doctor);
        savedAvailability.setStartAt(newStart);
        savedAvailability.setEndAt(newEnd);

        when(doctorRepository.findWithLockById(1L))
                .thenReturn(Optional.of(doctor));

        when(availabilityRepository
                .findByDoctorIdAndStartAtLessThanAndEndAtGreaterThan(
                        1L,
                        newEnd,
                        newStart))
                .thenReturn(List.of());

        when(availabilityRepository.saveAndFlush(
                any(DoctorAvailability.class)))
                .thenReturn(savedAvailability);

        // Act
        DoctorAvailabilityResponse response =
                availabilityService.create(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(101L, response.getId());
        Assertions.assertEquals(newStart, response.getStartAt());
        Assertions.assertEquals(newEnd, response.getEndAt());

        verify(availabilityRepository)
                .saveAndFlush(any(DoctorAvailability.class));
    }

    @Test
    void create_shouldLockDoctorBeforeCheckingForOverlap() {

        // Arrange
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endAt = startAt.plusHours(2);

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(1L);
        request.setStartAt(startAt);
        request.setEndAt(endAt);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setActive(true);

        DoctorAvailability savedAvailability =
                new DoctorAvailability();

        savedAvailability.setId(102L);
        savedAvailability.setDoctor(doctor);
        savedAvailability.setStartAt(startAt);
        savedAvailability.setEndAt(endAt);

        when(doctorRepository.findWithLockById(1L))
                .thenReturn(Optional.of(doctor));

        when(availabilityRepository
                .findByDoctorIdAndStartAtLessThanAndEndAtGreaterThan(
                        1L,
                        endAt,
                        startAt))
                .thenReturn(List.of());

        when(availabilityRepository.saveAndFlush(
                any(DoctorAvailability.class)))
                .thenReturn(savedAvailability);

        // Act
        availabilityService.create(request);

        // Assert
        InOrder inOrder = inOrder(
                doctorRepository,
                availabilityRepository
        );

        inOrder.verify(doctorRepository)
                .findWithLockById(1L);

        inOrder.verify(availabilityRepository)
                .findByDoctorIdAndStartAtLessThanAndEndAtGreaterThan(
                        1L,
                        endAt,
                        startAt);
    }

    @Test
    void getByDoctorId_shouldReturnDoctorAvailability() {

        // Arrange
        LocalDateTime startAt =
                LocalDateTime.now().plusDays(1);

        LocalDateTime endAt =
                startAt.plusHours(2);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setActive(true);

        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setId(200L);
        availability.setDoctor(doctor);
        availability.setStartAt(startAt);
        availability.setEndAt(endAt);

        when(doctorRepository.findByIdOrThrow(1L))
                .thenReturn(doctor);

        when(availabilityRepository.findByDoctorId(1L))
                .thenReturn(List.of(availability));

        // Act
        List<DoctorAvailabilityResponse> response =
                availabilityService.getByDoctorId(1L);

        // Assert
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(200L, response.get(0).getId());
        Assertions.assertEquals(1L, response.get(0).getDoctorId());
        Assertions.assertEquals(startAt, response.get(0).getStartAt());
        Assertions.assertEquals(endAt, response.get(0).getEndAt());
    }

    @Test
    void getByDoctorIdAndDateRange_shouldReturnOverlappingAvailability() {

        // Arrange
        LocalDateTime queryStart =
                LocalDateTime.of(2026, 9, 1, 9, 0);

        LocalDateTime queryEnd =
                LocalDateTime.of(2026, 9, 1, 17, 0);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setActive(true);

        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setId(300L);
        availability.setDoctor(doctor);
        availability.setStartAt(
                LocalDateTime.of(2026, 9, 1, 10, 0)
        );
        availability.setEndAt(
                LocalDateTime.of(2026, 9, 1, 12, 0)
        );

        when(doctorRepository.findByIdOrThrow(1L))
                .thenReturn(doctor);

        when(availabilityRepository
                .findByDoctorIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                        1L,
                        queryEnd,
                        queryStart))
                .thenReturn(List.of(availability));

        // Act
        List<DoctorAvailabilityResponse> response =
                availabilityService.getByDoctorIdAndDateRange(
                        1L,
                        queryStart,
                        queryEnd
                );

        // Assert
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(300L, response.get(0).getId());
        Assertions.assertEquals(1L, response.get(0).getDoctorId());
        Assertions.assertEquals(
                availability.getStartAt(),
                response.get(0).getStartAt()
        );
        Assertions.assertEquals(
                availability.getEndAt(),
                response.get(0).getEndAt()
        );

        verify(availabilityRepository)
                .findByDoctorIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                        1L,
                        queryEnd,
                        queryStart
                );
    }

    @Test
    void getByDoctorIdAndDateRange_shouldRejectInvalidRange() {

        // Arrange
        LocalDateTime startAt =
                LocalDateTime.of(2026, 9, 1, 17, 0);

        LocalDateTime endAt =
                LocalDateTime.of(2026, 9, 1, 9, 0);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setActive(true);

        when(doctorRepository.findByIdOrThrow(1L))
                .thenReturn(doctor);

        // Act & Assert
        ResponseStatusException exception =
                Assertions.assertThrows(
                        ResponseStatusException.class,
                        () -> availabilityService.getByDoctorIdAndDateRange(
                                1L,
                                startAt,
                                endAt
                        )
                );

        Assertions.assertEquals(
                400,
                exception.getStatusCode().value()
        );

        Assertions.assertEquals(
                "startAt must be before endAt",
                exception.getReason()
        );
    }

    @Test
    void delete_shouldDeleteAvailability() {

        // Arrange
        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setId(400L);

        when(availabilityRepository.findByIdOrThrow(400L))
                .thenReturn(availability);

        // Act
        availabilityService.delete(400L);

        // Assert
        verify(availabilityRepository)
                .findByIdOrThrow(400L);

        verify(availabilityRepository)
                .delete(availability);
    }
}