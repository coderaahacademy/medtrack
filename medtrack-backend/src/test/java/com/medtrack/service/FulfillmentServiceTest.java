package com.medtrack.service;

import com.medtrack.dto.CompleteFulfillmentRequest;
import com.medtrack.dto.FulfillPrescriptionItemRequest;
import com.medtrack.entity.Medication;
import com.medtrack.entity.Pharmacy;
import com.medtrack.entity.PharmacyInventory;
import com.medtrack.entity.Prescription;
import com.medtrack.entity.PrescriptionFulfillment;
import com.medtrack.entity.PrescriptionItem;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidStatusTransitionException;
import com.medtrack.repository.InventoryRepository;
import com.medtrack.repository.PrescriptionFulfillmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.medtrack.dto.FulfillmentResponse;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FulfillmentServiceTest {

    @Mock
    private PrescriptionFulfillmentRepository fulfillmentRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PrescriptionStatusTransitionService statusTransitionService;

    @Mock
    private PrescriptionAuditService auditService;

    @InjectMocks
    private FulfillmentService fulfillmentService;

    private PrescriptionFulfillment fulfillment;
    private Prescription prescription;

    @BeforeEach
    void setUp() {
        prescription = new Prescription();
        prescription.setId(1L);
        prescription.setStatus(PrescriptionStatus.CANCELLED);

        fulfillment = new PrescriptionFulfillment();
        fulfillment.setId(1L);
        fulfillment.setPrescription(prescription);
    }

    @Test
    void shouldRejectAcceptWhenPrescriptionIsCancelled() {
        when(fulfillmentRepository.findById(1L))
                .thenReturn(Optional.of(fulfillment));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> fulfillmentService.accept(1L)
        );
    }

    @Test
    void shouldRejectRejectWhenPrescriptionIsCancelled() {
        when(fulfillmentRepository.findById(1L))
                .thenReturn(Optional.of(fulfillment));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> fulfillmentService.reject(1L, null)
        );
    }

    @Test
    void shouldRejectPreparingWhenPrescriptionIsCancelled() {
        when(fulfillmentRepository.findById(1L))
                .thenReturn(Optional.of(fulfillment));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> fulfillmentService.preparing(1L)
        );
    }

    @Test
    void shouldRejectReadyWhenPrescriptionIsCancelled() {
        when(fulfillmentRepository.findById(1L))
                .thenReturn(Optional.of(fulfillment));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> fulfillmentService.ready(1L)
        );
    }

    @Test
    void shouldRejectCompletedWhenPrescriptionIsCancelled() {
        when(fulfillmentRepository.findForUpdate(1L))
                .thenReturn(Optional.of(fulfillment));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> fulfillmentService.completed(1L, null)
        );
    }

    @Test
    void shouldCompleteFulfillmentWhenAllItemsAreFullyDispensed() {

        Medication medication = new Medication();
        medication.setId(10L);

        PrescriptionItem item = new PrescriptionItem();
        item.setId(100L);
        item.setPrescription(prescription);
        item.setMedication(medication);
        item.setQuantity(10);
        item.setDispensedQuantity(0);

        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setItems(List.of(item));

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(20L);

        fulfillment.setPharmacy(pharmacy);
        fulfillment.setStatus(FulfillmentStatus.READY_FOR_PICKUP);

        PharmacyInventory inventory = new PharmacyInventory();
        inventory.setId(30L);
        inventory.setPharmacy(pharmacy);
        inventory.setMedication(medication);
        inventory.setQuantityAvailable(20);

        CompleteFulfillmentRequest request = new CompleteFulfillmentRequest();

        FulfillPrescriptionItemRequest requestItem =
                new FulfillPrescriptionItemRequest();

        requestItem.setPrescriptionItemId(100L);
        requestItem.setDispensedQuantity(10);

        request.setItems(List.of(requestItem));

        when(fulfillmentRepository.findForUpdate(1L))
                .thenReturn(Optional.of(fulfillment));

        when(inventoryRepository.findForUpdate(20L, 10L))
                .thenReturn(Optional.of(inventory));

        when(fulfillmentRepository.saveAndFlush(fulfillment))
                .thenReturn(fulfillment);

        FulfillmentResponse response =
                fulfillmentService.completed(1L, request);

        assertEquals(10, item.getDispensedQuantity());
        assertEquals(10, inventory.getQuantityAvailable());

        assertEquals(
                PrescriptionStatus.COMPLETED,
                prescription.getStatus()
        );

        assertEquals(
                FulfillmentStatus.COMPLETED,
                fulfillment.getStatus()
        );

        verify(inventoryRepository).save(inventory);
        verify(fulfillmentRepository).saveAndFlush(fulfillment);
    }

    @Test
    void shouldPartiallyFulfillWhenNotAllItemsAreDispensed() {

        Medication medication = new Medication();
        medication.setId(10L);

        PrescriptionItem item = new PrescriptionItem();
        item.setId(100L);
        item.setPrescription(prescription);
        item.setMedication(medication);
        item.setQuantity(10);
        item.setDispensedQuantity(0);

        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setItems(List.of(item));

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(20L);

        fulfillment.setPharmacy(pharmacy);
        fulfillment.setStatus(FulfillmentStatus.READY_FOR_PICKUP);

        PharmacyInventory inventory = new PharmacyInventory();
        inventory.setId(30L);
        inventory.setPharmacy(pharmacy);
        inventory.setMedication(medication);
        inventory.setQuantityAvailable(20);

        CompleteFulfillmentRequest request = new CompleteFulfillmentRequest();

        FulfillPrescriptionItemRequest requestItem =
                new FulfillPrescriptionItemRequest();

        requestItem.setPrescriptionItemId(100L);
        requestItem.setDispensedQuantity(4);

        request.setItems(List.of(requestItem));

        when(fulfillmentRepository.findForUpdate(1L))
                .thenReturn(Optional.of(fulfillment));

        when(inventoryRepository.findForUpdate(20L, 10L))
                .thenReturn(Optional.of(inventory));

        when(fulfillmentRepository.saveAndFlush(fulfillment))
                .thenReturn(fulfillment);

        fulfillmentService.completed(1L, request);

        assertEquals(4, item.getDispensedQuantity());
        assertEquals(16, inventory.getQuantityAvailable());

        assertEquals(
                PrescriptionStatus.PARTIALLY_FULFILLED,
                prescription.getStatus()
        );

        assertEquals(
                FulfillmentStatus.PARTIALLY_FULFILLED,
                fulfillment.getStatus()
        );

        verify(inventoryRepository).save(inventory);
        verify(fulfillmentRepository).saveAndFlush(fulfillment);
    }


    @Test
    void shouldCompletePartiallyFulfilledFulfillmentOnNextDispensing() {

        Medication medication = new Medication();
        medication.setId(10L);

        PrescriptionItem item = new PrescriptionItem();
        item.setId(100L);
        item.setPrescription(prescription);
        item.setMedication(medication);
        item.setQuantity(10);
        item.setDispensedQuantity(0);

        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setItems(List.of(item));

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(20L);

        fulfillment.setPharmacy(pharmacy);
        fulfillment.setStatus(FulfillmentStatus.READY_FOR_PICKUP);

        PharmacyInventory inventory = new PharmacyInventory();
        inventory.setId(30L);
        inventory.setPharmacy(pharmacy);
        inventory.setMedication(medication);
        inventory.setQuantityAvailable(20);

        CompleteFulfillmentRequest firstRequest =
                new CompleteFulfillmentRequest();

        FulfillPrescriptionItemRequest firstRequestItem =
                new FulfillPrescriptionItemRequest();

        firstRequestItem.setPrescriptionItemId(100L);
        firstRequestItem.setDispensedQuantity(4);

        firstRequest.setItems(List.of(firstRequestItem));

        when(fulfillmentRepository.findForUpdate(1L))
                .thenReturn(Optional.of(fulfillment));

        when(inventoryRepository.findForUpdate(20L, 10L))
                .thenReturn(Optional.of(inventory));

        when(fulfillmentRepository.saveAndFlush(fulfillment))
                .thenReturn(fulfillment);

        // First dispensing: 4 out of 10
        fulfillmentService.completed(1L, firstRequest);

        assertEquals(4, item.getDispensedQuantity());
        assertEquals(16, inventory.getQuantityAvailable());

        assertEquals(
                PrescriptionStatus.PARTIALLY_FULFILLED,
                prescription.getStatus()
        );

        assertEquals(
                FulfillmentStatus.PARTIALLY_FULFILLED,
                fulfillment.getStatus()
        );

        // Second dispensing: remaining 6
        CompleteFulfillmentRequest secondRequest =
                new CompleteFulfillmentRequest();

        FulfillPrescriptionItemRequest secondRequestItem =
                new FulfillPrescriptionItemRequest();

        secondRequestItem.setPrescriptionItemId(100L);
        secondRequestItem.setDispensedQuantity(6);

        secondRequest.setItems(List.of(secondRequestItem));

        fulfillmentService.completed(1L, secondRequest);

        assertEquals(10, item.getDispensedQuantity());
        assertEquals(10, inventory.getQuantityAvailable());

        assertEquals(
                PrescriptionStatus.COMPLETED,
                prescription.getStatus()
        );

        assertEquals(
                FulfillmentStatus.COMPLETED,
                fulfillment.getStatus()
        );

        verify(inventoryRepository, org.mockito.Mockito.times(2))
                .save(inventory);

        verify(fulfillmentRepository, org.mockito.Mockito.times(2))
                .saveAndFlush(fulfillment);
    }

    @Test
    void shouldRejectDispensedQuantityGreaterThanPrescribedQuantity() {

        Medication medication = new Medication();
        medication.setId(10L);

        PrescriptionItem item = new PrescriptionItem();
        item.setId(100L);
        item.setPrescription(prescription);
        item.setMedication(medication);
        item.setQuantity(10);
        item.setDispensedQuantity(0);

        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setItems(List.of(item));

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(20L);

        fulfillment.setPharmacy(pharmacy);
        fulfillment.setStatus(FulfillmentStatus.READY_FOR_PICKUP);

        PharmacyInventory inventory = new PharmacyInventory();
        inventory.setId(30L);
        inventory.setPharmacy(pharmacy);
        inventory.setMedication(medication);
        inventory.setQuantityAvailable(20);

        CompleteFulfillmentRequest request =
                new CompleteFulfillmentRequest();

        FulfillPrescriptionItemRequest requestItem =
                new FulfillPrescriptionItemRequest();

        requestItem.setPrescriptionItemId(100L);
        requestItem.setDispensedQuantity(11);

        request.setItems(List.of(requestItem));

        when(fulfillmentRepository.findForUpdate(1L))
                .thenReturn(Optional.of(fulfillment));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.completed(1L, request)
        );

        assertEquals(
                "Dispensed quantity cannot exceed prescribed quantity for prescription item id 100",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectCompletionWhenInventoryIsInsufficient() {

        Medication medication = new Medication();
        medication.setId(10L);

        PrescriptionItem item = new PrescriptionItem();
        item.setId(100L);
        item.setPrescription(prescription);
        item.setMedication(medication);
        item.setQuantity(10);
        item.setDispensedQuantity(0);

        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setItems(List.of(item));

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(20L);

        fulfillment.setPharmacy(pharmacy);
        fulfillment.setStatus(FulfillmentStatus.READY_FOR_PICKUP);

        PharmacyInventory inventory = new PharmacyInventory();
        inventory.setId(30L);
        inventory.setPharmacy(pharmacy);
        inventory.setMedication(medication);
        inventory.setQuantityAvailable(5);

        CompleteFulfillmentRequest request =
                new CompleteFulfillmentRequest();

        FulfillPrescriptionItemRequest requestItem =
                new FulfillPrescriptionItemRequest();

        requestItem.setPrescriptionItemId(100L);
        requestItem.setDispensedQuantity(10);

        request.setItems(List.of(requestItem));

        when(fulfillmentRepository.findForUpdate(1L))
                .thenReturn(Optional.of(fulfillment));

        when(inventoryRepository.findForUpdate(20L, 10L))
                .thenReturn(Optional.of(inventory));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.completed(1L, request)
        );

        assertEquals(
                "Insufficient inventory for medication id 10",
                exception.getMessage()
        );

        assertEquals(5, inventory.getQuantityAvailable());
    }

    @Test
    void shouldMarkPrescriptionPartiallyFulfilledWhenOneOfMultipleItemsIsPartiallyDispensed() {

        Medication medication1 = new Medication();
        medication1.setId(10L);

        Medication medication2 = new Medication();
        medication2.setId(11L);

        PrescriptionItem item1 = new PrescriptionItem();
        item1.setId(100L);
        item1.setPrescription(prescription);
        item1.setMedication(medication1);
        item1.setQuantity(10);
        item1.setDispensedQuantity(0);

        PrescriptionItem item2 = new PrescriptionItem();
        item2.setId(101L);
        item2.setPrescription(prescription);
        item2.setMedication(medication2);
        item2.setQuantity(20);
        item2.setDispensedQuantity(0);

        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setItems(List.of(item1, item2));

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(20L);

        fulfillment.setPharmacy(pharmacy);
        fulfillment.setStatus(FulfillmentStatus.READY_FOR_PICKUP);

        PharmacyInventory inventory1 = new PharmacyInventory();
        inventory1.setId(30L);
        inventory1.setPharmacy(pharmacy);
        inventory1.setMedication(medication1);
        inventory1.setQuantityAvailable(20);

        PharmacyInventory inventory2 = new PharmacyInventory();
        inventory2.setId(31L);
        inventory2.setPharmacy(pharmacy);
        inventory2.setMedication(medication2);
        inventory2.setQuantityAvailable(20);

        CompleteFulfillmentRequest request =
                new CompleteFulfillmentRequest();

        FulfillPrescriptionItemRequest requestItem1 =
                new FulfillPrescriptionItemRequest();
        requestItem1.setPrescriptionItemId(100L);
        requestItem1.setDispensedQuantity(10);

        FulfillPrescriptionItemRequest requestItem2 =
                new FulfillPrescriptionItemRequest();
        requestItem2.setPrescriptionItemId(101L);
        requestItem2.setDispensedQuantity(5);

        request.setItems(List.of(requestItem1, requestItem2));

        when(fulfillmentRepository.findForUpdate(1L))
                .thenReturn(Optional.of(fulfillment));

        when(inventoryRepository.findForUpdate(20L, 10L))
                .thenReturn(Optional.of(inventory1));

        when(inventoryRepository.findForUpdate(20L, 11L))
                .thenReturn(Optional.of(inventory2));

        when(fulfillmentRepository.saveAndFlush(fulfillment))
                .thenReturn(fulfillment);

        fulfillmentService.completed(1L, request);

        assertEquals(
                PrescriptionStatus.PARTIALLY_FULFILLED,
                prescription.getStatus()
        );

        assertEquals(10, item1.getDispensedQuantity());
        assertEquals(5, item2.getDispensedQuantity());

        assertEquals(10, inventory1.getQuantityAvailable());
        assertEquals(15, inventory2.getQuantityAvailable());
    }


    @Test
    void shouldMarkPrescriptionCompletedWhenAllMultipleItemsAreFullyDispensed() {

        Medication medication1 = new Medication();
        medication1.setId(10L);

        Medication medication2 = new Medication();
        medication2.setId(11L);

        PrescriptionItem item1 = new PrescriptionItem();
        item1.setId(100L);
        item1.setPrescription(prescription);
        item1.setMedication(medication1);
        item1.setQuantity(10);
        item1.setDispensedQuantity(0);

        PrescriptionItem item2 = new PrescriptionItem();
        item2.setId(101L);
        item2.setPrescription(prescription);
        item2.setMedication(medication2);
        item2.setQuantity(20);
        item2.setDispensedQuantity(0);

        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setItems(List.of(item1, item2));

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(20L);

        fulfillment.setPharmacy(pharmacy);
        fulfillment.setStatus(FulfillmentStatus.READY_FOR_PICKUP);

        PharmacyInventory inventory1 = new PharmacyInventory();
        inventory1.setId(30L);
        inventory1.setPharmacy(pharmacy);
        inventory1.setMedication(medication1);
        inventory1.setQuantityAvailable(20);

        PharmacyInventory inventory2 = new PharmacyInventory();
        inventory2.setId(31L);
        inventory2.setPharmacy(pharmacy);
        inventory2.setMedication(medication2);
        inventory2.setQuantityAvailable(30);

        CompleteFulfillmentRequest request =
                new CompleteFulfillmentRequest();

        FulfillPrescriptionItemRequest requestItem1 =
                new FulfillPrescriptionItemRequest();
        requestItem1.setPrescriptionItemId(100L);
        requestItem1.setDispensedQuantity(10);

        FulfillPrescriptionItemRequest requestItem2 =
                new FulfillPrescriptionItemRequest();
        requestItem2.setPrescriptionItemId(101L);
        requestItem2.setDispensedQuantity(20);

        request.setItems(List.of(requestItem1, requestItem2));

        when(fulfillmentRepository.findForUpdate(1L))
                .thenReturn(Optional.of(fulfillment));

        when(inventoryRepository.findForUpdate(20L, 10L))
                .thenReturn(Optional.of(inventory1));

        when(inventoryRepository.findForUpdate(20L, 11L))
                .thenReturn(Optional.of(inventory2));

        when(fulfillmentRepository.saveAndFlush(fulfillment))
                .thenReturn(fulfillment);

        fulfillmentService.completed(1L, request);

        assertEquals(
                PrescriptionStatus.COMPLETED,
                prescription.getStatus()
        );

        assertEquals(10, item1.getDispensedQuantity());
        assertEquals(20, item2.getDispensedQuantity());

        assertEquals(10, inventory1.getQuantityAvailable());
        assertEquals(10, inventory2.getQuantityAvailable());
    }

    @Test
    void shouldRejectAllZeroDispensingOperation() {

        Medication medication = new Medication();
        medication.setId(10L);

        PrescriptionItem item = new PrescriptionItem();
        item.setId(100L);
        item.setPrescription(prescription);
        item.setMedication(medication);
        item.setQuantity(10);
        item.setDispensedQuantity(0);

        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setItems(List.of(item));

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(20L);

        fulfillment.setPharmacy(pharmacy);
        fulfillment.setStatus(FulfillmentStatus.READY_FOR_PICKUP);

        CompleteFulfillmentRequest request =
                new CompleteFulfillmentRequest();

        FulfillPrescriptionItemRequest requestItem =
                new FulfillPrescriptionItemRequest();

        requestItem.setPrescriptionItemId(100L);
        requestItem.setDispensedQuantity(0);

        request.setItems(List.of(requestItem));

        when(fulfillmentRepository.findForUpdate(1L))
                .thenReturn(Optional.of(fulfillment));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fulfillmentService.completed(1L, request)
        );

        assertEquals(
                "Fulfillment operation must dispense at least one item",
                exception.getMessage()
        );

        assertEquals(0, item.getDispensedQuantity());
    }
}
