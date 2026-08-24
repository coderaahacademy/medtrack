package com.medtrack.service;

import com.medtrack.dto.CreatePharmacyRequest;
import com.medtrack.dto.PharmacyRequest;
import com.medtrack.dto.PharmacyResponse;
import com.medtrack.entity.Pharmacy;
import com.medtrack.entity.User;
import com.medtrack.enums.Role;
import com.medtrack.repository.PharmacyRepository;
import com.medtrack.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PharmacyService {
    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;

    public PharmacyService(PharmacyRepository pharmacyRepository, UserRepository userRepository) {
        this.pharmacyRepository = pharmacyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PharmacyResponse create(CreatePharmacyRequest request) {
        Long userId = request.getUserId();
        User user = userRepository.findByIdOrThrow(userId);
        if (pharmacyRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pharmacy profile already exists for user ID: " + userId);
        }
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(request.getName());
        pharmacy.setAddress(request.getAddress());
        pharmacy.setPhone(request.getPhone());
        pharmacy.setEmail(request.getEmail());
        pharmacy.setActive(request.isActive());
        user.addRole(Role.PHARMACY);
        userRepository.saveAndFlush(user);
        pharmacy.setUser(user);
        return toResponse(pharmacyRepository.saveAndFlush(pharmacy));
    }

    public Page<PharmacyResponse> getAll(Pageable pageable){
        return pharmacyRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PharmacyResponse getById(Long id) {
        return toResponse(pharmacyRepository.findByIdOrThrow(id));
    }

    @Transactional
    public PharmacyResponse update(Long id, PharmacyRequest request) {
        Pharmacy pharmacy = pharmacyRepository.findByIdOrThrow(id);

        // Ordinary owners cannot reactivate a pharmacy profile deactivated by an administrator
        if (!pharmacy.isActive() && request.isActive()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new AccessDeniedException("Only administrators can reactivate a deactivated pharmacy profile");
            }
        }

        pharmacy.setName(request.getName());
        pharmacy.setAddress(request.getAddress());
        pharmacy.setPhone(request.getPhone());
        pharmacy.setEmail(request.getEmail());
        pharmacy.setActive(request.isActive());
        pharmacyRepository.saveAndFlush(pharmacy);
        return toResponse(pharmacy);
    }

    @Transactional
    public PharmacyResponse deactivate(Long id) {
        Pharmacy pharmacy = pharmacyRepository.findByIdOrThrow(id);
        pharmacy.setActive(false);
        pharmacyRepository.saveAndFlush(pharmacy);
        return toResponse(pharmacy);
    }

    private PharmacyResponse toResponse(Pharmacy pharmacy) {
        PharmacyResponse response = new PharmacyResponse();
        response.setName(pharmacy.getName());
        response.setAddress(pharmacy.getAddress());
        response.setPhone(pharmacy.getPhone());
        response.setEmail(pharmacy.getEmail());
        response.setActive(pharmacy.isActive());
        response.setCreatedAt(pharmacy.getCreatedAt());
        response.setUpdatedAt(pharmacy.getUpdatedAt());
        response.setId(pharmacy.getId());
        response.setUserId(pharmacy.getUser() != null ? pharmacy.getUser().getId() : null);
        return response;
    }
}