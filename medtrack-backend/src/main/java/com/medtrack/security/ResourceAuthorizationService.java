package com.medtrack.security;

import com.medtrack.entity.*;
import com.medtrack.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import com.medtrack.repository.DoctorAvailabilityRepository;

/**
 * Central Resource Authorization Service for MedTrack.
 *
 * Provides a single authoritative place for evaluating role membership and resource ownership
 * across patients, doctors, pharmacies, prescriptions, fulfillments, visits, and medical reports.
 *
 * Used in method-level security expressions (e.g., @PreAuthorize("@authz.ownsPatient(#id)"))
 * and programmatic security checks.
 */
@Service("authz")
@Transactional(readOnly = true)
public class ResourceAuthorizationService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionFulfillmentRepository fulfillmentRepository;
    private final VisitRepository visitRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;

    public ResourceAuthorizationService(PatientRepository patientRepository,
                                        DoctorRepository doctorRepository,
                                        DoctorAvailabilityRepository doctorAvailabilityRepository,
                                        PharmacyRepository pharmacyRepository,
                                        PrescriptionRepository prescriptionRepository,
                                        PrescriptionFulfillmentRepository fulfillmentRepository,
                                        VisitRepository visitRepository) {
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.fulfillmentRepository = fulfillmentRepository;
        this.visitRepository = visitRepository;
    }

    // ==========================================
    // AUTHENTICATED USER & ROLE HELPERS
    // ==========================================

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public MedTrackUserDetails getCurrentUserDetails() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof MedTrackUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    public Long currentUserId() {
        MedTrackUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null ? userDetails.getId() : null;
    }

    public boolean hasRole(String role) {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(roleWithPrefix));
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isDoctor() {
        return hasRole("ROLE_DOCTOR");
    }

    public boolean isPatient() {
        return hasRole("ROLE_PATIENT");
    }

    public boolean isPharmacy() {
        return hasRole("ROLE_PHARMACY");
    }

    public boolean isCurrentUserId(Long userId) {
        return userId != null && userId.equals(currentUserId());
    }

    // ==========================================
    // PROFILE CREATION AUTHORIZATION
    // ==========================================

    public boolean canCreatePatient(Long userId) {
        if (isAdmin()) {
            return true;
        }
        return isPatient() && isCurrentUserId(userId);
    }

    public boolean canCreateDoctor(Long userId) {
        if (isAdmin()) {
            return true;
        }
        return isDoctor() && isCurrentUserId(userId);
    }

    public boolean canCreatePharmacy(Long userId) {
        if (isAdmin()) {
            return true;
        }
        return isPharmacy() && isCurrentUserId(userId);
    }
    public boolean canManageDoctorAvailabilityById(Long availabilityId) {
        if (availabilityId == null) {
            return false;
        }

        var availability = doctorAvailabilityRepository.findByIdOrThrow(availabilityId);

        if (isAdmin()) {
            return true;
        }

        return isDoctor()
                && availability.getDoctor() != null
                && ownsDoctor(availability.getDoctor().getId());
    }

    // ==========================================
    // PROFILE OWNERSHIP & ACCESS AUTHORIZATION
    // ==========================================

    public boolean ownsPatient(Long patientId) {
        if (patientId == null) return false;
        Patient patient = patientRepository.findByIdOrThrow(patientId);
        return patient.getUser() != null && isCurrentUserId(patient.getUser().getId());
    }

    public boolean ownsDoctor(Long doctorId) {
        if (doctorId == null) return false;
        Doctor doctor = doctorRepository.findByIdOrThrow(doctorId);
        return doctor.getUser() != null && isCurrentUserId(doctor.getUser().getId());
    }

    public boolean ownsPharmacy(Long pharmacyId) {
        if (pharmacyId == null) return false;
        Pharmacy pharmacy = pharmacyRepository.findByIdOrThrow(pharmacyId);
        return pharmacy.getUser() != null && isCurrentUserId(pharmacy.getUser().getId());
    }

    public boolean isFamilyDoctorForPatient(Long patientId) {
        if (patientId == null) return false;
        Patient patient = patientRepository.findByIdOrThrow(patientId);
        return patient.getFamilyDoctor() != null
                && patient.getFamilyDoctor().getUser() != null
                && isCurrentUserId(patient.getFamilyDoctor().getUser().getId());
    }

    public boolean canAccessPatient(Long patientId) {
        if (patientId == null) return false;
        if (isAdmin()) {
            // Confirm entity exists so non-existent IDs still trigger 404
            patientRepository.findByIdOrThrow(patientId);
            return true;
        }
        return ownsPatient(patientId) || isFamilyDoctorForPatient(patientId);
    }

    public boolean canAccessPatientPrescriptions(Long patientId) {
        return canAccessPatient(patientId);
    }

    public boolean canAccessPatientTimeline(Long patientId) {
        return canAccessPatient(patientId);
    }

    public boolean canAccessPatientVisits(Long patientId) {
        return canAccessPatient(patientId);
    }

    public boolean canModifyPatient(Long patientId) {
        if (patientId == null) return false;
        if (isAdmin()) {
            patientRepository.findByIdOrThrow(patientId);
            return true;
        }
        return ownsPatient(patientId);
    }

    public boolean canModifyDoctor(Long doctorId) {
        if (doctorId == null) return false;
        if (isAdmin()) {
            doctorRepository.findByIdOrThrow(doctorId);
            return true;
        }
        return ownsDoctor(doctorId);
    }
    public boolean canManageDoctorAvailability(Long doctorId) {
        if (doctorId == null) {
            return false;
        }

        if (isAdmin()) {
            doctorRepository.findByIdOrThrow(doctorId);
            return true;
        }

        return isDoctor() && ownsDoctor(doctorId);
    }

    public boolean canReadDoctorAvailability(Long doctorId) {
        if (doctorId == null) {
            return false;
        }

        Doctor doctor = doctorRepository.findByIdOrThrow(doctorId);

        if (!doctor.isActive()) {
            return false;
        }

        if (isAdmin()) {
            return true;
        }

        if (isPatient()) {
            return true;
        }

        return isDoctor() && ownsDoctor(doctorId);
    }

    public boolean canModifyPharmacy(Long pharmacyId) {
        if (pharmacyId == null) return false;
        if (isAdmin()) {
            pharmacyRepository.findByIdOrThrow(pharmacyId);
            return true;
        }
        return ownsPharmacy(pharmacyId);
    }

    public boolean canAccessPharmacyInventory(Long pharmacyId) {
        if (pharmacyId == null) return false;
        if (isAdmin()) {
            pharmacyRepository.findByIdOrThrow(pharmacyId);
            return true;
        }
        return isPharmacy() && ownsPharmacy(pharmacyId);
    }

    // ==========================================
    // CLINICAL RESOURCE: PRESCRIPTIONS
    // ==========================================

    public boolean canCreatePrescription(Long doctorId) {
        if (doctorId == null || !isDoctor()) {
            return false;
        }
        return ownsDoctor(doctorId);
    }

    public boolean isPrescribingDoctor(Long prescriptionId) {
        if (prescriptionId == null) return false;
        Prescription prescription = prescriptionRepository.findByIdOrThrow(prescriptionId);
        return prescription.getDoctor() != null
                && prescription.getDoctor().getUser() != null
                && isCurrentUserId(prescription.getDoctor().getUser().getId());
    }

    public boolean isPrescriptionPatient(Long prescriptionId) {
        if (prescriptionId == null) return false;
        Prescription prescription = prescriptionRepository.findByIdOrThrow(prescriptionId);
        return prescription.getPatient() != null
                && prescription.getPatient().getUser() != null
                && isCurrentUserId(prescription.getPatient().getUser().getId());
    }

    public boolean isAssignedPharmacyForPrescription(Long prescriptionId) {
        if (prescriptionId == null) return false;
        Optional<PrescriptionFulfillment> fulfillment = fulfillmentRepository.findByPrescriptionId(prescriptionId);
        return fulfillment.isPresent()
                && fulfillment.get().getPharmacy() != null
                && fulfillment.get().getPharmacy().getUser() != null
                && isCurrentUserId(fulfillment.get().getPharmacy().getUser().getId());
    }

    public boolean canReadPrescription(Long prescriptionId) {
        if (prescriptionId == null) return false;
        if (isAdmin()) {
            prescriptionRepository.findByIdOrThrow(prescriptionId);
            return true;
        }
        return isPrescriptionPatient(prescriptionId)
                || isPrescribingDoctor(prescriptionId)
                || isAssignedPharmacyForPrescription(prescriptionId);
    }

    public boolean canReadPrescriptionAudit(Long prescriptionId) {
        return canReadPrescription(prescriptionId);
    }

    public boolean canCancelPrescription(Long prescriptionId) {
        return isDoctor() && isPrescribingDoctor(prescriptionId);
    }

    public boolean canSendToPharmacy(Long prescriptionId) {
        return isDoctor() && isPrescribingDoctor(prescriptionId);
    }

    // ==========================================
    // CLINICAL RESOURCE: FULFILLMENTS
    // ==========================================

    public boolean ownsFulfillment(Long fulfillmentId) {
        if (fulfillmentId == null) return false;
        PrescriptionFulfillment fulfillment = fulfillmentRepository.findByIdOrThrow(fulfillmentId);
        return fulfillment.getPharmacy() != null
                && fulfillment.getPharmacy().getUser() != null
                && isCurrentUserId(fulfillment.getPharmacy().getUser().getId());
    }

    public boolean canModifyFulfillment(Long fulfillmentId) {
        return isPharmacy() && ownsFulfillment(fulfillmentId);
    }

    // ==========================================
    // CLINICAL RESOURCE: VISITS
    // ==========================================

    public boolean canCreateVisit(Long doctorId) {
        return isDoctor() && doctorId != null && ownsDoctor(doctorId);
    }

    public boolean isTreatingDoctorForVisit(Long visitId) {
        if (visitId == null) return false;
        Visit visit = visitRepository.findByIdOrThrow(visitId);
        return visit.getDoctor() != null
                && visit.getDoctor().getUser() != null
                && isCurrentUserId(visit.getDoctor().getUser().getId());
    }

    public boolean isVisitPatient(Long visitId) {
        if (visitId == null) return false;
        Visit visit = visitRepository.findByIdOrThrow(visitId);
        return visit.getPatient() != null
                && visit.getPatient().getUser() != null
                && isCurrentUserId(visit.getPatient().getUser().getId());
    }

    public boolean canAddVisitNotes(Long visitId) {
        return isDoctor() && isTreatingDoctorForVisit(visitId);
    }

    public boolean canReadVisitNotes(Long visitId) {
        if (visitId == null) return false;
        if (isAdmin()) {
            visitRepository.findByIdOrThrow(visitId);
            return true;
        }
        Visit visit = visitRepository.findByIdOrThrow(visitId);
        if (visit.getPatient() != null && visit.getPatient().getUser() != null
                && isCurrentUserId(visit.getPatient().getUser().getId())) {
            return true;
        }
        if (visit.getDoctor() != null && visit.getDoctor().getUser() != null
                && isCurrentUserId(visit.getDoctor().getUser().getId())) {
            return true;
        }
        if (visit.getPatient() != null && isFamilyDoctorForPatient(visit.getPatient().getId())) {
            return true;
        }
        return false;
    }

    // ==========================================
    // CLINICAL RESOURCE: MEDICAL REPORTS
    // ==========================================

    public boolean canCreateMedicalReport(Long doctorId) {
        if (isAdmin()) {
            return true;
        }
        if (isDoctor()) {
            return doctorId != null && ownsDoctor(doctorId);
        }
        return false;
    }
}
