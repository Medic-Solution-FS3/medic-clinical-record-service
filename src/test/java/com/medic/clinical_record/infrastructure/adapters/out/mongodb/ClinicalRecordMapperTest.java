package com.medic.clinical_record.infrastructure.adapters.out.mongodb;

import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.domain.model.Prescription;
import com.medic.clinical_record.infrastructure.adapters.out.mongodb.entity.ClinicalRecordDocument;
import com.medic.clinical_record.infrastructure.adapters.out.mongodb.entity.PrescriptionDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ClinicalRecordMapper")
class ClinicalRecordMapperTest {

    private final ClinicalRecordMapper mapper = new ClinicalRecordMapperImpl();

    private static final Instant CREATED_AT = Instant.parse("2024-06-01T10:15:30Z");
    private static final Instant UPDATED_AT = Instant.parse("2024-06-02T08:00:00Z");

    @Nested
    @DisplayName("toDocument(Prescription)")
    class PrescriptionToDocument {

        @Test
        @DisplayName("maps all fields")
        void shouldMapAllFields() {
            Prescription prescription = new Prescription(
                    "Amoxicillin", "500 mg every 8 hours", "Take with food", LocalDate.of(2024, 6, 1));

            PrescriptionDocument document = mapper.toDocument(prescription);

            assertThat(document.getMedicationName()).isEqualTo("Amoxicillin");
            assertThat(document.getDosage()).isEqualTo("500 mg every 8 hours");
            assertThat(document.getInstructions()).isEqualTo("Take with food");
            assertThat(document.getPrescribedDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        }

        @Test
        @DisplayName("returns null when given null")
        void shouldReturnNullForNullInput() {
            assertThat(mapper.toDocument((Prescription) null)).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain(PrescriptionDocument)")
    class PrescriptionToDomain {

        @Test
        @DisplayName("maps all fields")
        void shouldMapAllFields() {
            PrescriptionDocument document = new PrescriptionDocument();
            document.setMedicationName("Ibuprofen");
            document.setDosage("200 mg twice daily");
            document.setInstructions("Take after meals");
            document.setPrescribedDate(LocalDate.of(2024, 5, 20));

            Prescription prescription = mapper.toDomain(document);

            assertThat(prescription.medicationName()).isEqualTo("Ibuprofen");
            assertThat(prescription.dosage()).isEqualTo("200 mg twice daily");
            assertThat(prescription.instructions()).isEqualTo("Take after meals");
            assertThat(prescription.prescribedDate()).isEqualTo(LocalDate.of(2024, 5, 20));
        }

        @Test
        @DisplayName("returns null when given null")
        void shouldReturnNullForNullInput() {
            assertThat(mapper.toDomain((PrescriptionDocument) null)).isNull();
        }
    }

    @Nested
    @DisplayName("toDocument(ClinicalRecord)")
    class ClinicalRecordToDocument {

        @Test
        @DisplayName("maps all fields including nested prescriptions")
        void shouldMapAllFieldsWithPrescriptions() {
            Prescription prescription = new Prescription(
                    "Amoxicillin", "500 mg every 8 hours", "Take with food", LocalDate.of(2024, 6, 1));
            ClinicalRecord record = ClinicalRecord.reconstitute(
                    "rec-1", "patient-1", CREATED_AT, UPDATED_AT, List.of(prescription));

            ClinicalRecordDocument document = mapper.toDocument(record);

            assertThat(document.getId()).isEqualTo("rec-1");
            assertThat(document.getPatientId()).isEqualTo("patient-1");
            assertThat(document.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(document.getUpdatedAt()).isEqualTo(UPDATED_AT);
            assertThat(document.getPrescriptions()).hasSize(1);
            assertThat(document.getPrescriptions().get(0).getMedicationName()).isEqualTo("Amoxicillin");
        }

        @Test
        @DisplayName("maps an empty prescription list")
        void shouldMapEmptyPrescriptionList() {
            ClinicalRecord record = ClinicalRecord.reconstitute(
                    "rec-2", "patient-2", CREATED_AT, UPDATED_AT, List.of());

            ClinicalRecordDocument document = mapper.toDocument(record);

            assertThat(document.getPrescriptions()).isEmpty();
        }

        @Test
        @DisplayName("returns null when given null")
        void shouldReturnNullForNullInput() {
            assertThat(mapper.toDocument((ClinicalRecord) null)).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain(ClinicalRecordDocument)")
    class ClinicalRecordToDomain {

        @Test
        @DisplayName("maps all fields including nested prescriptions")
        void shouldMapAllFieldsWithPrescriptions() {
            PrescriptionDocument prescriptionDocument = new PrescriptionDocument();
            prescriptionDocument.setMedicationName("Ibuprofen");
            prescriptionDocument.setDosage("200 mg twice daily");
            prescriptionDocument.setInstructions("Take after meals");
            prescriptionDocument.setPrescribedDate(LocalDate.of(2024, 5, 20));

            ClinicalRecordDocument document = new ClinicalRecordDocument();
            document.setId("rec-3");
            document.setPatientId("patient-3");
            document.setCreatedAt(CREATED_AT);
            document.setUpdatedAt(UPDATED_AT);
            document.setPrescriptions(List.of(prescriptionDocument));

            ClinicalRecord record = mapper.toDomain(document);

            assertThat(record.id()).isEqualTo("rec-3");
            assertThat(record.patientId()).isEqualTo("patient-3");
            assertThat(record.createdAt()).isEqualTo(CREATED_AT);
            assertThat(record.updatedAt()).isEqualTo(UPDATED_AT);
            assertThat(record.prescriptions()).hasSize(1);
            assertThat(record.prescriptions().get(0).medicationName()).isEqualTo("Ibuprofen");
        }

        @Test
        @DisplayName("maps a null prescription list to an empty list")
        void shouldMapNullPrescriptionListToEmptyList() {
            ClinicalRecordDocument document = new ClinicalRecordDocument();
            document.setId("rec-4");
            document.setPatientId("patient-4");
            document.setCreatedAt(CREATED_AT);
            document.setUpdatedAt(UPDATED_AT);
            document.setPrescriptions(null);

            ClinicalRecord record = mapper.toDomain(document);

            assertThat(record.prescriptions()).isEmpty();
        }

        @Test
        @DisplayName("returns null when given null")
        void shouldReturnNullForNullInput() {
            assertThat(mapper.toDomain((ClinicalRecordDocument) null)).isNull();
        }
    }
}
