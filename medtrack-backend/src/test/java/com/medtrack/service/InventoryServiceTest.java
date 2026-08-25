package com.medtrack.service;

import com.medtrack.dto.CreateInventoryRequest;
import com.medtrack.dto.UpdateInventoryRequest;
import com.medtrack.entity.Medication;
import com.medtrack.entity.Pharmacy;
import com.medtrack.entity.PharmacyInventory;
import com.medtrack.repository.InventoryRepository;
import com.medtrack.repository.MedicationRepository;
import com.medtrack.repository.PharmacyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private PharmacyRepository pharmacyRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Pharmacy pharmacy;
    private Medication medication;

    @BeforeEach
    void setUp() {
        pharmacy = new Pharmacy();
        pharmacy.setId(1L);
        pharmacy.setName("Test Pharmacy");
        pharmacy.setActive(true);

        medication = new Medication();
        medication.setId(1L);
        medication.setName("Test Medication");
        medication.setActive(true);
    }

    @Test
    void shouldRejectInventoryForInactivePharmacy() {
        pharmacy.setActive(false);

        CreateInventoryRequest request = createRequest();

        when(inventoryRepository.existsByPharmacyIdAndMedicationId(1L, 1L))
                .thenReturn(false);

        when(pharmacyRepository.findByIdOrThrow(1L))
                .thenReturn(pharmacy);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inventoryService.create(1L, request)
        );

        assertEquals(409, exception.getStatusCode().value());

        verify(medicationRepository, never()).findByIdOrThrow(anyLong());
        verify(inventoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectInventoryForInactiveMedication() {
        medication.setActive(false);

        CreateInventoryRequest request = createRequest();

        when(inventoryRepository.existsByPharmacyIdAndMedicationId(1L, 1L))
                .thenReturn(false);

        when(pharmacyRepository.findByIdOrThrow(1L))
                .thenReturn(pharmacy);

        when(medicationRepository.findByIdOrThrow(1L))
                .thenReturn(medication);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inventoryService.create(1L, request)
        );

        assertEquals(409, exception.getStatusCode().value());

        verify(inventoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectDuplicateInventory() {
        CreateInventoryRequest request = createRequest();

        when(inventoryRepository.existsByPharmacyIdAndMedicationId(1L, 1L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inventoryService.create(1L, request)
        );

        assertEquals(409, exception.getStatusCode().value());

        verify(pharmacyRepository, never()).findByIdOrThrow(anyLong());
        verify(medicationRepository, never()).findByIdOrThrow(anyLong());
        verify(inventoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldCreateInventoryForActivePharmacyAndMedication() {
        CreateInventoryRequest request = createRequest();

        PharmacyInventory inventory = new PharmacyInventory();
        inventory.setId(10L);
        inventory.setPharmacy(pharmacy);
        inventory.setMedication(medication);
        inventory.setQuantityAvailable(20);
        inventory.setMinimumStock(5);

        when(inventoryRepository.existsByPharmacyIdAndMedicationId(1L, 1L))
                .thenReturn(false);

        when(pharmacyRepository.findByIdOrThrow(1L))
                .thenReturn(pharmacy);

        when(medicationRepository.findByIdOrThrow(1L))
                .thenReturn(medication);

        when(inventoryRepository.saveAndFlush(any(PharmacyInventory.class)))
                .thenReturn(inventory);

        var response = inventoryService.create(1L, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getPharmacyId());
        assertEquals(1L, response.getMedicationId());
        assertEquals(20, response.getQuantityAvailable());
        assertEquals(5, response.getMinimumStock());
    }

    @Test
    void shouldUpdateExistingInventory() {
        PharmacyInventory inventory = new PharmacyInventory();
        inventory.setId(10L);
        inventory.setPharmacy(pharmacy);
        inventory.setMedication(medication);
        inventory.setQuantityAvailable(20);
        inventory.setMinimumStock(5);

        UpdateInventoryRequest request = new UpdateInventoryRequest();
        request.setQuantityAvailable(8);
        request.setMinimumStock(3);

        /*
         * InventoryService.update() uses findForUpdate()
         * to lock the inventory row before updating it.
         */
        when(inventoryRepository.findForUpdate(1L, 1L))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.saveAndFlush(inventory))
                .thenReturn(inventory);

        var response = inventoryService.update(1L, 1L, request);

        assertNotNull(response);
        assertEquals(8, response.getQuantityAvailable());
        assertEquals(3, response.getMinimumStock());

        verify(inventoryRepository).findForUpdate(1L, 1L);
        verify(inventoryRepository).saveAndFlush(inventory);
    }

    @Test
    void shouldRejectUpdateWhenInventoryDoesNotExist() {
        UpdateInventoryRequest request = new UpdateInventoryRequest();
        request.setQuantityAvailable(8);
        request.setMinimumStock(3);

        when(inventoryRepository.findForUpdate(1L, 1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inventoryService.update(1L, 1L, request)
        );

        assertEquals(404, exception.getStatusCode().value());

        verify(inventoryRepository).findForUpdate(1L, 1L);
        verify(inventoryRepository, never()).saveAndFlush(any());
    }

    private CreateInventoryRequest createRequest() {
        CreateInventoryRequest request = new CreateInventoryRequest();

        request.setMedicationId(1L);
        request.setQuantityAvailable(20);
        request.setMinimumStock(5);

        return request;
    }
}