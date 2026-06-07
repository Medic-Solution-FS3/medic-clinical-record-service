package com.medic.clinical_record.application.services;

import com.medic.clinical_record.application.ports.in.CreateClinicalRecordUseCase;
import com.medic.clinical_record.application.ports.out.ClinicalRecordEventPublisherPort;
import com.medic.clinical_record.application.ports.out.ClinicalRecordRepositoryPort;
import com.medic.clinical_record.domain.ClinicalRecordAlreadyExistsException;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateClinicalRecordService")
class CreateClinicalRecordServiceTest {

    @Mock
    private ClinicalRecordRepositoryPort repositoryPort;

    @Mock
    private ClinicalRecordEventPublisherPort eventPublisherPort;

    @InjectMocks
    private CreateClinicalRecordService service;

    private static final String PATIENT_ID = "patient-abc-123";

    private ClinicalRecord stubSaved() {
        return new ClinicalRecord("cr-generated-id", PATIENT_ID);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("execute(patientId) — happy path")
    class HappyPath {

        @Test
        @DisplayName("returns the ClinicalRecord returned by the repository")
        void shouldReturnPersistedRecord() {
            ClinicalRecord saved = stubSaved();
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.empty());
            when(repositoryPort.save(any(ClinicalRecord.class))).thenReturn(saved);

            ClinicalRecord result = service.execute(PATIENT_ID);

            assertThat(result).isNotNull().isSameAs(saved);
        }

        @Test
        @DisplayName("saves a new ClinicalRecord carrying the given patientId")
        void shouldSaveRecordWithCorrectPatientId() {
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.empty());
            when(repositoryPort.save(any(ClinicalRecord.class))).thenReturn(stubSaved());

            service.execute(PATIENT_ID);

            ArgumentCaptor<ClinicalRecord> captor = ArgumentCaptor.forClass(ClinicalRecord.class);
            verify(repositoryPort).save(captor.capture());
            assertThat(captor.getValue().patientId()).isEqualTo(PATIENT_ID);
        }

        @Test
        @DisplayName("publishes a record-created event with the persisted record")
        void shouldPublishEventWithPersistedRecord() {
            ClinicalRecord saved = stubSaved();
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.empty());
            when(repositoryPort.save(any(ClinicalRecord.class))).thenReturn(saved);

            service.execute(PATIENT_ID);

            verify(eventPublisherPort).publishRecordCreatedEvent(saved);
        }

        @Test
        @DisplayName("persists before publishing (ordering guarantee)")
        void shouldSaveBeforePublishing() {
            ClinicalRecord saved = stubSaved();
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.empty());
            when(repositoryPort.save(any(ClinicalRecord.class))).thenReturn(saved);

            service.execute(PATIENT_ID);

            InOrder order = inOrder(repositoryPort, eventPublisherPort);
            order.verify(repositoryPort).save(any(ClinicalRecord.class));
            order.verify(eventPublisherPort).publishRecordCreatedEvent(any(ClinicalRecord.class));
        }

        @Test
        @DisplayName("invokes save and publish exactly once per call")
        void shouldInvokeSaveAndPublishExactlyOnce() {
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.empty());
            when(repositoryPort.save(any(ClinicalRecord.class))).thenReturn(stubSaved());

            service.execute(PATIENT_ID);

            verify(repositoryPort, times(1)).save(any(ClinicalRecord.class));
            verify(eventPublisherPort, times(1)).publishRecordCreatedEvent(any(ClinicalRecord.class));
        }
    }

    // -------------------------------------------------------------------------
    // Duplicate record guard
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("execute(patientId) — duplicate record")
    class DuplicateRecord {

        @Test
        @DisplayName("throws ClinicalRecordAlreadyExistsException when a record already exists for the patient")
        void shouldThrowWhenRecordAlreadyExists() {
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(stubSaved()));

            assertThatThrownBy(() -> service.execute(PATIENT_ID))
                    .isInstanceOf(ClinicalRecordAlreadyExistsException.class);
        }

        @Test
        @DisplayName("exception message contains the duplicate patientId")
        void shouldIncludePatientIdInExceptionMessage() {
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(stubSaved()));

            assertThatThrownBy(() -> service.execute(PATIENT_ID))
                    .isInstanceOf(ClinicalRecordAlreadyExistsException.class)
                    .hasMessageContaining(PATIENT_ID);
        }

        @Test
        @DisplayName("never calls save when a duplicate is detected")
        void shouldNotSaveWhenDuplicate() {
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(stubSaved()));

            assertThatThrownBy(() -> service.execute(PATIENT_ID))
                    .isInstanceOf(ClinicalRecordAlreadyExistsException.class);

            verify(repositoryPort, never()).save(any(ClinicalRecord.class));
        }

        @Test
        @DisplayName("never publishes an event when a duplicate is detected")
        void shouldNotPublishWhenDuplicate() {
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(stubSaved()));

            assertThatThrownBy(() -> service.execute(PATIENT_ID))
                    .isInstanceOf(ClinicalRecordAlreadyExistsException.class);

            verifyNoInteractions(eventPublisherPort);
        }
    }

    // -------------------------------------------------------------------------
    // Structural contract
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("structural contract")
    class StructuralContract {

        @Test
        @DisplayName("service implements CreateClinicalRecordUseCase")
        void shouldImplementUseCase() {
            assertThat(service).isInstanceOf(CreateClinicalRecordUseCase.class);
        }
    }
}
