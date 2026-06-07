package com.medic.clinical_record.application.services;

import com.medic.clinical_record.application.ports.in.GetClinicalRecordUseCase;
import com.medic.clinical_record.application.ports.out.ClinicalRecordRepositoryPort;
import com.medic.clinical_record.domain.ClinicalRecordNotFoundException;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetClinicalRecordService")
class GetClinicalRecordServiceTest {

    @Mock
    private ClinicalRecordRepositoryPort repositoryPort;

    @InjectMocks
    private GetClinicalRecordService service;

    private static final String PATIENT_ID         = "patient-abc-123";
    private static final String UNKNOWN_PATIENT_ID = "patient-unknown-999";

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("execute(patientId) — happy path")
    class HappyPath {

        @Test
        @DisplayName("returns the exact ClinicalRecord instance found by the repository")
        void shouldReturnFoundRecord() {
            ClinicalRecord existing = new ClinicalRecord("cr-001", PATIENT_ID);
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(existing));

            ClinicalRecord result = service.execute(PATIENT_ID);

            assertThat(result).isSameAs(existing);
        }

        @Test
        @DisplayName("delegates the lookup to the repository using the given patientId")
        void shouldDelegateLookupToRepository() {
            ClinicalRecord existing = new ClinicalRecord("cr-001", PATIENT_ID);
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(existing));

            service.execute(PATIENT_ID);

            verify(repositoryPort, times(1)).findByPatientId(PATIENT_ID);
        }
    }

    // -------------------------------------------------------------------------
    // Not found
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("execute(patientId) — record not found")
    class NotFound {

        @Test
        @DisplayName("throws ClinicalRecordNotFoundException when no record exists for the patient")
        void shouldThrowWhenRecordNotFound() {
            when(repositoryPort.findByPatientId(UNKNOWN_PATIENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(UNKNOWN_PATIENT_ID))
                    .isInstanceOf(ClinicalRecordNotFoundException.class);
        }

        @Test
        @DisplayName("exception message contains the patientId that was not found")
        void shouldIncludePatientIdInExceptionMessage() {
            when(repositoryPort.findByPatientId(UNKNOWN_PATIENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(UNKNOWN_PATIENT_ID))
                    .isInstanceOf(ClinicalRecordNotFoundException.class)
                    .hasMessageContaining(UNKNOWN_PATIENT_ID);
        }
    }

    // -------------------------------------------------------------------------
    // Structural contract
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("structural contract")
    class StructuralContract {

        @Test
        @DisplayName("service implements GetClinicalRecordUseCase")
        void shouldImplementUseCase() {
            assertThat(service).isInstanceOf(GetClinicalRecordUseCase.class);
        }
    }
}
