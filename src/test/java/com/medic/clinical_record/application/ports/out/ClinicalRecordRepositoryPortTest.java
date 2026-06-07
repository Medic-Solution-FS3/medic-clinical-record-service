package com.medic.clinical_record.application.ports.out;

import com.medic.clinical_record.domain.model.ClinicalRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClinicalRecordRepositoryPort — outbound persistence contract")
class ClinicalRecordRepositoryPortTest {

    @Mock
    private ClinicalRecordRepositoryPort repositoryPort;

    private static final String PATIENT_ID    = "patient-abc-123";
    private static final String UNKNOWN_ID    = "patient-unknown-999";
    private static final String RECORD_ID     = "cr-001";

    private ClinicalRecord buildRecord() {
        return new ClinicalRecord(RECORD_ID, PATIENT_ID);
    }

    // -------------------------------------------------------------------------
    // save()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("save(ClinicalRecord)")
    class Save {

        @Test
        @DisplayName("returns the persisted ClinicalRecord")
        void shouldReturnPersistedRecord() {
            ClinicalRecord record = buildRecord();
            when(repositoryPort.save(record)).thenReturn(record);

            ClinicalRecord result = repositoryPort.save(record);

            assertThat(result).isSameAs(record);
            verify(repositoryPort).save(record);
        }

        @Test
        @DisplayName("method signature: (ClinicalRecord) → ClinicalRecord")
        void shouldDeclareCorrectSignature() throws NoSuchMethodException {
            Method save = ClinicalRecordRepositoryPort.class
                    .getMethod("save", ClinicalRecord.class);

            assertThat(save.getReturnType())
                    .as("save() must return ClinicalRecord")
                    .isEqualTo(ClinicalRecord.class);
        }
    }

    // -------------------------------------------------------------------------
    // findByPatientId()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findByPatientId(String)")
    class FindByPatientId {

        @Test
        @DisplayName("returns Optional containing the record when the patient exists")
        void shouldReturnPresentOptionalWhenFound() {
            ClinicalRecord record = buildRecord();
            when(repositoryPort.findByPatientId(PATIENT_ID)).thenReturn(Optional.of(record));

            Optional<ClinicalRecord> result = repositoryPort.findByPatientId(PATIENT_ID);

            assertThat(result).isPresent().contains(record);
            verify(repositoryPort).findByPatientId(PATIENT_ID);
        }

        @Test
        @DisplayName("returns Optional.empty() when the patient does not exist")
        void shouldReturnEmptyOptionalWhenNotFound() {
            when(repositoryPort.findByPatientId(UNKNOWN_ID)).thenReturn(Optional.empty());

            Optional<ClinicalRecord> result = repositoryPort.findByPatientId(UNKNOWN_ID);

            assertThat(result).isEmpty();
            verify(repositoryPort).findByPatientId(UNKNOWN_ID);
        }

        @Test
        @DisplayName("method signature: (String) → Optional<ClinicalRecord>")
        void shouldDeclareCorrectSignature() throws NoSuchMethodException {
            Method find = ClinicalRecordRepositoryPort.class
                    .getMethod("findByPatientId", String.class);

            assertThat(find.getReturnType())
                    .as("findByPatientId() must return Optional")
                    .isEqualTo(Optional.class);
            assertThat(find.getGenericReturnType().getTypeName())
                    .as("findByPatientId() must be parameterised with ClinicalRecord")
                    .contains("ClinicalRecord");
        }
    }

    // -------------------------------------------------------------------------
    // Framework agnosticism
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("framework agnosticism")
    class FrameworkAgnosticism {

        @Test
        @DisplayName("carries no Spring or Spring Data annotations")
        void shouldHaveNoSpringAnnotations() {
            boolean contaminated = Arrays.stream(ClinicalRecordRepositoryPort.class.getAnnotations())
                    .anyMatch(a -> a.annotationType().getName().startsWith("org.springframework"));

            assertThat(contaminated)
                    .as("ClinicalRecordRepositoryPort must not carry Spring annotations")
                    .isFalse();
        }

        @Test
        @DisplayName("does not extend any Spring Data repository interface")
        void shouldNotExtendSpringDataRepository() {
            boolean extendsSpringData = Arrays.stream(ClinicalRecordRepositoryPort.class.getInterfaces())
                    .anyMatch(i -> i.getName().startsWith("org.springframework.data"));

            assertThat(extendsSpringData)
                    .as("ClinicalRecordRepositoryPort must not extend Spring Data interfaces")
                    .isFalse();
        }

        @Test
        @DisplayName("save() parameter belongs to the domain model package")
        void shouldAcceptOnlyDomainTypes() throws NoSuchMethodException {
            Method save = ClinicalRecordRepositoryPort.class
                    .getMethod("save", ClinicalRecord.class);

            String paramPackage = save.getParameterTypes()[0].getPackageName();

            assertThat(paramPackage)
                    .as("save() must operate on domain model types")
                    .startsWith("com.medic.clinical_record.domain");
        }
    }
}
