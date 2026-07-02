package com.medic.clinical_record.infrastructure.adapters.out.mongodb;

import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.infrastructure.adapters.out.mongodb.entity.ClinicalRecordDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MongoClinicalRecordRepositoryAdapter")
class MongoClinicalRecordRepositoryAdapterTest {

    @Mock
    private SpringDataMongoClinicalRecordRepository mongoRepository;

    @Mock
    private ClinicalRecordMapper mapper;

    private MongoClinicalRecordRepositoryAdapter adapter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        adapter = new MongoClinicalRecordRepositoryAdapter(mongoRepository, mapper);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("maps to document, persists it, and maps the saved document back to domain")
        void shouldPersistAndReturnDomainRecord() {
            ClinicalRecord toSave = new ClinicalRecord("rec-1", "patient-1");
            ClinicalRecordDocument documentToSave = new ClinicalRecordDocument();
            ClinicalRecordDocument savedDocument = new ClinicalRecordDocument();
            ClinicalRecord savedRecord = new ClinicalRecord("rec-1", "patient-1");

            given(mapper.toDocument(toSave)).willReturn(documentToSave);
            given(mongoRepository.save(documentToSave)).willReturn(savedDocument);
            given(mapper.toDomain(savedDocument)).willReturn(savedRecord);

            ClinicalRecord result = adapter.save(toSave);

            assertThat(result).isSameAs(savedRecord);
            verify(mongoRepository).save(documentToSave);
        }
    }

    @Nested
    @DisplayName("findByPatientId")
    class FindByPatientId {

        @Test
        @DisplayName("returns the mapped record when found")
        void shouldReturnMappedRecordWhenFound() {
            ClinicalRecordDocument document = new ClinicalRecordDocument();
            ClinicalRecord domainRecord = new ClinicalRecord("rec-1", "patient-1");

            given(mongoRepository.findByPatientId("patient-1")).willReturn(Optional.of(document));
            given(mapper.toDomain(document)).willReturn(domainRecord);

            Optional<ClinicalRecord> result = adapter.findByPatientId("patient-1");

            assertThat(result).contains(domainRecord);
        }

        @Test
        @DisplayName("returns empty when no document exists for the patient")
        void shouldReturnEmptyWhenNotFound() {
            given(mongoRepository.findByPatientId("unknown")).willReturn(Optional.empty());

            Optional<ClinicalRecord> result = adapter.findByPatientId("unknown");

            assertThat(result).isEmpty();
        }
    }
}
