package com.medic.clinical_record.domain;

import com.medic.clinical_record.domain.model.Prescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("Prescription")
class PrescriptionTest {

    private static final String MEDICATION  = "Amoxicillin";
    private static final String DOSAGE      = "500 mg every 8 hours";
    private static final String INSTRUCTIONS = "Take with food and a full glass of water";
    private static final LocalDate DATE     = LocalDate.of(2024, 6, 1);

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when created with valid data")
    class ValidCreation {

        @Test
        @DisplayName("stores all provided fields")
        void shouldStoreAllFields() {
            Prescription p = new Prescription(MEDICATION, DOSAGE, INSTRUCTIONS, DATE);

            assertThat(p.medicationName()).isEqualTo(MEDICATION);
            assertThat(p.dosage()).isEqualTo(DOSAGE);
            assertThat(p.instructions()).isEqualTo(INSTRUCTIONS);
            assertThat(p.prescribedDate()).isEqualTo(DATE);
        }

        @Test
        @DisplayName("allows null instructions (optional field)")
        void shouldAllowNullInstructions() {
            assertThatNoException()
                    .isThrownBy(() -> new Prescription(MEDICATION, DOSAGE, null, DATE));
        }

        @Test
        @DisplayName("allows blank instructions (optional field)")
        void shouldAllowBlankInstructions() {
            assertThatNoException()
                    .isThrownBy(() -> new Prescription(MEDICATION, DOSAGE, "  ", DATE));
        }
    }

    // -------------------------------------------------------------------------
    // medicationName invariants
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("medicationName invariant")
    class MedicationNameInvariant {

        @Test
        @DisplayName("rejects null medicationName")
        void shouldThrowWhenNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Prescription(null, DOSAGE, INSTRUCTIONS, DATE))
                    .withMessageContaining("medicationName");
        }

        @Test
        @DisplayName("rejects empty medicationName")
        void shouldThrowWhenEmpty() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Prescription("", DOSAGE, INSTRUCTIONS, DATE))
                    .withMessageContaining("medicationName");
        }

        @Test
        @DisplayName("rejects blank medicationName")
        void shouldThrowWhenBlank() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Prescription("   ", DOSAGE, INSTRUCTIONS, DATE))
                    .withMessageContaining("medicationName");
        }
    }

    // -------------------------------------------------------------------------
    // dosage invariants
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("dosage invariant")
    class DosageInvariant {

        @Test
        @DisplayName("rejects null dosage")
        void shouldThrowWhenNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Prescription(MEDICATION, null, INSTRUCTIONS, DATE))
                    .withMessageContaining("dosage");
        }

        @Test
        @DisplayName("rejects empty dosage")
        void shouldThrowWhenEmpty() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Prescription(MEDICATION, "", INSTRUCTIONS, DATE))
                    .withMessageContaining("dosage");
        }

        @Test
        @DisplayName("rejects blank dosage")
        void shouldThrowWhenBlank() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Prescription(MEDICATION, "   ", INSTRUCTIONS, DATE))
                    .withMessageContaining("dosage");
        }
    }

    // -------------------------------------------------------------------------
    // prescribedDate invariant
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("prescribedDate invariant")
    class PrescribedDateInvariant {

        @Test
        @DisplayName("rejects null prescribedDate")
        void shouldThrowWhenNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Prescription(MEDICATION, DOSAGE, INSTRUCTIONS, null))
                    .withMessageContaining("prescribedDate");
        }
    }

    // -------------------------------------------------------------------------
    // Value-object equality
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("value equality")
    class ValueEquality {

        @Test
        @DisplayName("two prescriptions with identical fields are equal")
        void shouldBeEqualWhenFieldsMatch() {
            Prescription p1 = new Prescription(MEDICATION, DOSAGE, INSTRUCTIONS, DATE);
            Prescription p2 = new Prescription(MEDICATION, DOSAGE, INSTRUCTIONS, DATE);

            assertThat(p1).isEqualTo(p2);
        }

        @Test
        @DisplayName("equal prescriptions have the same hashCode")
        void shouldHaveSameHashCodeWhenEqual() {
            Prescription p1 = new Prescription(MEDICATION, DOSAGE, INSTRUCTIONS, DATE);
            Prescription p2 = new Prescription(MEDICATION, DOSAGE, INSTRUCTIONS, DATE);

            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("prescriptions with different medicationName are not equal")
        void shouldNotBeEqualWhenMedicationDiffers() {
            Prescription p1 = new Prescription(MEDICATION,  DOSAGE, INSTRUCTIONS, DATE);
            Prescription p2 = new Prescription("Ibuprofen", DOSAGE, INSTRUCTIONS, DATE);

            assertThat(p1).isNotEqualTo(p2);
        }
    }

    // -------------------------------------------------------------------------
    // Framework-agnostic structural check
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("has no Spring or Jakarta imports at runtime (framework-agnostic)")
    void shouldHaveNoFrameworkAnnotations() {
        Class<?> clazz = Prescription.class;

        boolean hasSpringAnnotation = java.util.Arrays.stream(clazz.getAnnotations())
                .anyMatch(a -> a.annotationType().getName().startsWith("org.springframework")
                        || a.annotationType().getName().startsWith("jakarta.validation")
                        || a.annotationType().getName().startsWith("org.springframework.data"));

        assertThat(hasSpringAnnotation)
                .as("Prescription must be framework-agnostic")
                .isFalse();
    }
}
