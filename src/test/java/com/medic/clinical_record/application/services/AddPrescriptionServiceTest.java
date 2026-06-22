package com.medic.clinical_record.application.services;

import com.medic.clinical_record.application.ports.in.AddPrescriptionUseCase;
import com.medic.clinical_record.application.ports.out.ClinicalRecordRepositoryPort;
import com.medic.clinical_record.domain.ClinicalRecordNotFoundException;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.domain.model.Prescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddPrescriptionService")
class AddPrescriptionServiceTest {

    @Mock
    private ClinicalRecordRepositoryPort repositoryPort;

    @InjectMocks
    private AddPrescriptionService service;

    private static final String PATIENT_ID         = "patient-abc-123";
    private static final String UNKNOWN_PATIENT_ID = "patient-unknown-999";

    private Prescription buildPrescription() {
        return new Prescription("Ibuprofen", "400mg", "Take with food", LocalDate.of(2026, 6, 6));
    }

    private ClinicalRecord buildRecord() {
        return new ClinicalRecord("cr-001", PATIENT_ID);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("execute(patientId, prescription) — happy path")
    class HappyPath {

        @Test
        @DisplayName("returns the record produced by the repository after saving")
        void shouldReturnSavedRecord() {
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(buildRecord()));
            when(repositoryPort.save(any(ClinicalRecord.class))).thenAnswer(inv -> inv.getArgument(0));

            ClinicalRecord result = service.execute(PATIENT_ID, buildPrescription());

            assertThat(result).isNotNull();
            assertThat(result.patientId()).isEqualTo(PATIENT_ID);
        }

        @Test
        @DisplayName("saves a record that contains the new prescription")
        void shouldSaveRecordContainingThePrescription() {
            Prescription prescription = buildPrescription();
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(buildRecord()));
            when(repositoryPort.save(any(ClinicalRecord.class))).thenAnswer(inv -> inv.getArgument(0));

            service.execute(PATIENT_ID, prescription);

            ArgumentCaptor<ClinicalRecord> captor = ArgumentCaptor.forClass(ClinicalRecord.class);
            verify(repositoryPort).save(captor.capture());
            assertThat(captor.getValue().prescriptions()).containsExactly(prescription);
        }

        @Test
        @DisplayName("does not mutate the original record — delegates addition to the domain aggregate")
        void shouldNotMutateOriginalRecord() {
            ClinicalRecord existing = buildRecord();
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(existing));
            when(repositoryPort.save(any(ClinicalRecord.class))).thenAnswer(inv -> inv.getArgument(0));

            service.execute(PATIENT_ID, buildPrescription());

            assertThat(existing.prescriptions())
                    .as("The original ClinicalRecord must remain unmodified after execute()")
                    .isEmpty();
        }

        @Test
        @DisplayName("invokes save exactly once with the updated record")
        void shouldInvokeSaveExactlyOnce() {
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(buildRecord()));
            when(repositoryPort.save(any(ClinicalRecord.class))).thenAnswer(inv -> inv.getArgument(0));

            service.execute(PATIENT_ID, buildPrescription());

            verify(repositoryPort, times(1)).save(any(ClinicalRecord.class));
        }
    }

    // -------------------------------------------------------------------------
    // Record not found
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("execute(patientId, prescription) — record not found")
    class RecordNotFound {

        @Test
        @DisplayName("throws ClinicalRecordNotFoundException when no record exists for the patient")
        void shouldThrowWhenRecordNotFound() {
            when(repositoryPort.findByPatientId(UNKNOWN_PATIENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(UNKNOWN_PATIENT_ID, buildPrescription()))
                    .isInstanceOf(ClinicalRecordNotFoundException.class);
        }

        @Test
        @DisplayName("never calls save when the record is not found")
        void shouldNotSaveWhenRecordNotFound() {
            when(repositoryPort.findByPatientId(UNKNOWN_PATIENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(UNKNOWN_PATIENT_ID, buildPrescription()))
                    .isInstanceOf(ClinicalRecordNotFoundException.class);

            verify(repositoryPort, never()).save(any(ClinicalRecord.class));
        }
    }

    // -------------------------------------------------------------------------
    // Structural contract
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("structural contract")
    class StructuralContract {

        @Test
        @DisplayName("service implements AddPrescriptionUseCase")
        void shouldImplementUseCase() {
            assertThat(service).isInstanceOf(AddPrescriptionUseCase.class);
        }
    }
}
