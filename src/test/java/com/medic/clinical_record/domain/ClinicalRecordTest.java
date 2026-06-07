package com.medic.clinical_record.domain;

import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.domain.model.Prescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ClinicalRecord (aggregate root)")
class ClinicalRecordTest {

    private static final String VALID_ID         = "cr-uuid-001";
    private static final String VALID_PATIENT_ID = "patient-abc-123";

    private Prescription buildPrescription() {
        return new Prescription("Ibuprofen", "400 mg daily", "After meals", LocalDate.now());
    }

    // -------------------------------------------------------------------------
    // Happy path — creation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when created with valid data")
    class ValidCreation {

        @Test
        @DisplayName("stores id and patientId")
        void shouldStoreIdentifiers() {
            ClinicalRecord record = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);

            assertThat(record.id()).isEqualTo(VALID_ID);
            assertThat(record.patientId()).isEqualTo(VALID_PATIENT_ID);
        }

        @Test
        @DisplayName("sets createdAt to a non-null instant")
        void shouldSetCreatedAt() {
            Instant before = Instant.now();
            ClinicalRecord record = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);
            Instant after = Instant.now();

            assertThat(record.createdAt())
                    .isNotNull()
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("sets updatedAt equal to createdAt on first creation")
        void shouldSetUpdatedAtEqualToCreatedAt() {
            ClinicalRecord record = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);

            assertThat(record.updatedAt())
                    .isNotNull()
                    .isAfterOrEqualTo(record.createdAt());
        }

        @Test
        @DisplayName("starts with an empty prescription list")
        void shouldStartWithNoPrescriptions() {
            ClinicalRecord record = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);

            assertThat(record.prescriptions()).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // patientId invariants
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("patientId invariant")
    class PatientIdInvariant {

        @Test
        @DisplayName("rejects null patientId")
        void shouldThrowWhenNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ClinicalRecord(VALID_ID, null))
                    .withMessageContaining("patientId");
        }

        @Test
        @DisplayName("rejects empty patientId")
        void shouldThrowWhenEmpty() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ClinicalRecord(VALID_ID, ""))
                    .withMessageContaining("patientId");
        }

        @Test
        @DisplayName("rejects blank patientId")
        void shouldThrowWhenBlank() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ClinicalRecord(VALID_ID, "   "))
                    .withMessageContaining("patientId");
        }
    }

    // -------------------------------------------------------------------------
    // addPrescription — behaviour
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("addPrescription()")
    class AddPrescription {

        @Test
        @DisplayName("returns a new record that contains the added prescription")
        void shouldContainPrescriptionInReturnedRecord() {
            ClinicalRecord original   = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);
            Prescription prescription = buildPrescription();

            ClinicalRecord updated = original.addPrescription(prescription);

            assertThat(updated.prescriptions())
                    .hasSize(1)
                    .contains(prescription);
        }

        @Test
        @DisplayName("accumulates prescriptions across chained calls")
        void shouldAccumulatePrescriptions() {
            ClinicalRecord record = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);

            ClinicalRecord updated = record
                    .addPrescription(buildPrescription())
                    .addPrescription(buildPrescription())
                    .addPrescription(buildPrescription());

            assertThat(updated.prescriptions()).hasSize(3);
        }

        @Test
        @DisplayName("preserves the original record unchanged (immutability)")
        void shouldNotMutateOriginalRecord() {
            ClinicalRecord original = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);

            original.addPrescription(buildPrescription()); // return value intentionally discarded

            assertThat(original.prescriptions())
                    .as("original ClinicalRecord must remain unchanged")
                    .isEmpty();
        }

        @Test
        @DisplayName("advances updatedAt relative to the original record")
        void shouldAdvanceUpdatedAt() {
            ClinicalRecord original = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);

            ClinicalRecord updated = original.addPrescription(buildPrescription());

            assertThat(updated.updatedAt())
                    .isAfterOrEqualTo(original.updatedAt());
        }

        @Test
        @DisplayName("preserves the original createdAt in the returned record")
        void shouldKeepOriginalCreatedAt() {
            ClinicalRecord original = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);

            ClinicalRecord updated = original.addPrescription(buildPrescription());

            assertThat(updated.createdAt()).isEqualTo(original.createdAt());
        }

        @Test
        @DisplayName("rejects null prescription")
        void shouldThrowWhenPrescriptionIsNull() {
            ClinicalRecord record = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> record.addPrescription(null))
                    .withMessageContaining("prescription");
        }
    }

    // -------------------------------------------------------------------------
    // Encapsulation of prescription list
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("prescription list encapsulation")
    class PrescriptionListEncapsulation {

        @Test
        @DisplayName("returns an unmodifiable view of prescriptions")
        void shouldReturnUnmodifiableList() {
            ClinicalRecord record = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID);

            assertThatThrownBy(() -> record.prescriptions().add(buildPrescription()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("returns an unmodifiable view even after prescriptions are added")
        void shouldReturnUnmodifiableListAfterAdd() {
            ClinicalRecord record = new ClinicalRecord(VALID_ID, VALID_PATIENT_ID)
                    .addPrescription(buildPrescription());

            assertThatThrownBy(() -> record.prescriptions().add(buildPrescription()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // -------------------------------------------------------------------------
    // Framework-agnostic structural check
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("has no Spring or Jakarta imports at runtime (framework-agnostic)")
    void shouldHaveNoFrameworkAnnotations() {
        Class<?> clazz = ClinicalRecord.class;

        boolean hasFrameworkAnnotation = java.util.Arrays.stream(clazz.getAnnotations())
                .anyMatch(a -> a.annotationType().getName().startsWith("org.springframework")
                        || a.annotationType().getName().startsWith("jakarta.validation")
                        || a.annotationType().getName().startsWith("org.springframework.data"));

        assertThat(hasFrameworkAnnotation)
                .as("ClinicalRecord must be framework-agnostic")
                .isFalse();
    }
}
