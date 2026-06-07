package com.medic.clinical_record.infrastructure.adapters.out.mongodb;

import com.medic.clinical_record.application.ports.out.ClinicalRecordEventPublisherPort;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.domain.model.Prescription;
import com.medic.clinical_record.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MongoClinicalRecordRepositoryAdapter — integration tests")
class MongoClinicalRecordRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private MongoClinicalRecordRepositoryAdapter adapter;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockitoBean
    private ClinicalRecordEventPublisherPort eventPublisherPort;

    @AfterEach
    void cleanDatabase() {
        mongoTemplate.getDb().drop();
    }

    private Prescription buildPrescription() {
        return new Prescription("Ibuprofen", "400mg", "Take with food", LocalDate.of(2026, 6, 6));
    }

    // -------------------------------------------------------------------------
    // save()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("save(ClinicalRecord)")
    class Save {

        @Test
        @DisplayName("persists a new clinical record and returns it with the patientId intact")
        void shouldPersistAndReturnRecord() {
            ClinicalRecord record = new ClinicalRecord(null, "patient-001");

            ClinicalRecord saved = adapter.save(record);

            assertThat(saved).isNotNull();
            assertThat(saved.patientId()).isEqualTo("patient-001");
        }

        @Test
        @DisplayName("assigns a non-null identifier to the persisted record")
        void shouldAssignIdOnSave() {
            ClinicalRecord record = new ClinicalRecord(null, "patient-002");

            ClinicalRecord saved = adapter.save(record);

            assertThat(saved.id()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("persists prescriptions alongside the clinical record")
        void shouldPersistPrescriptions() {
            ClinicalRecord record = new ClinicalRecord(null, "patient-003")
                    .addPrescription(buildPrescription());

            ClinicalRecord saved = adapter.save(record);

            assertThat(saved.prescriptions()).hasSize(1);
            assertThat(saved.prescriptions().getFirst().medicationName()).isEqualTo("Ibuprofen");
        }
    }

    // -------------------------------------------------------------------------
    // findByPatientId()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findByPatientId(String)")
    class FindByPatientId {

        @Test
        @DisplayName("returns the record after it has been saved")
        void shouldFindSavedRecord() {
            adapter.save(new ClinicalRecord(null, "patient-004"));

            Optional<ClinicalRecord> found = adapter.findByPatientId("patient-004");

            assertThat(found).isPresent();
            assertThat(found.get().patientId()).isEqualTo("patient-004");
        }

        @Test
        @DisplayName("returns Optional.empty() when no record exists for the patientId")
        void shouldReturnEmptyWhenNotFound() {
            Optional<ClinicalRecord> found = adapter.findByPatientId("patient-unknown");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("retrieves prescriptions that were persisted with the record")
        void shouldRetrievePersistedPrescriptions() {
            ClinicalRecord record = new ClinicalRecord(null, "patient-005")
                    .addPrescription(buildPrescription());
            adapter.save(record);

            Optional<ClinicalRecord> found = adapter.findByPatientId("patient-005");

            assertThat(found).isPresent();
            assertThat(found.get().prescriptions()).hasSize(1);
            assertThat(found.get().prescriptions().getFirst().medicationName()).isEqualTo("Ibuprofen");
        }

        @Test
        @DisplayName("does not return a record belonging to a different patient")
        void shouldNotReturnRecordForDifferentPatient() {
            adapter.save(new ClinicalRecord(null, "patient-006"));

            Optional<ClinicalRecord> found = adapter.findByPatientId("patient-007");

            assertThat(found).isEmpty();
        }
    }
}
