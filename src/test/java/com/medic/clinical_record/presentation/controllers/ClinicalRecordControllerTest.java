package com.medic.clinical_record.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medic.clinical_record.application.ports.in.CreateClinicalRecordUseCase;
import com.medic.clinical_record.application.ports.in.GetClinicalRecordUseCase;
import com.medic.clinical_record.domain.ClinicalRecordAlreadyExistsException;
import com.medic.clinical_record.domain.ClinicalRecordNotFoundException;
import com.medic.clinical_record.domain.model.ClinicalRecord;
import com.medic.clinical_record.presentation.dtos.CreateClinicalRecordRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClinicalRecordController.class)
@DisplayName("ClinicalRecordController")
class ClinicalRecordControllerTest {

    private static final String BASE_URL   = "/api/v1/clinical-records";
    private static final String PATIENT_ID = "patient-abc-123";
    private static final String RECORD_ID  = "cr-001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateClinicalRecordUseCase createClinicalRecordUseCase;

    @MockitoBean
    private GetClinicalRecordUseCase getClinicalRecordUseCase;

    // =========================================================================
    // POST /api/v1/clinical-records
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/clinical-records — successful creation")
    class SuccessfulCreation {

        @Test
        @DisplayName("returns 201 Created with patientId in response body")
        void shouldReturn201WithPatientId() throws Exception {
            ClinicalRecord record = new ClinicalRecord(RECORD_ID, PATIENT_ID);
            when(createClinicalRecordUseCase.execute(PATIENT_ID)).thenReturn(record);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.patientId").value(PATIENT_ID));
        }

        @Test
        @DisplayName("returns 201 Created with id in response body")
        void shouldReturn201WithId() throws Exception {
            ClinicalRecord record = new ClinicalRecord(RECORD_ID, PATIENT_ID);
            when(createClinicalRecordUseCase.execute(PATIENT_ID)).thenReturn(record);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(RECORD_ID));
        }

        @Test
        @DisplayName("delegates to CreateClinicalRecordUseCase with the patientId from the request")
        void shouldDelegateToUseCaseWithCorrectPatientId() throws Exception {
            ClinicalRecord record = new ClinicalRecord(RECORD_ID, PATIENT_ID);
            when(createClinicalRecordUseCase.execute(PATIENT_ID)).thenReturn(record);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isCreated());

            verify(createClinicalRecordUseCase, times(1)).execute(PATIENT_ID);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/clinical-records — request validation failures")
    class RequestValidationFailures {

        @Test
        @DisplayName("returns 400 Bad Request when patientId is blank")
        void shouldReturn400WhenPatientIdIsBlank() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(""))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 Bad Request when body is empty JSON object")
        void shouldReturn400WhenBodyHasNoPatientId() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("response body contains a fieldErrors array pointing to the patientId field")
        void shouldIncludeFieldErrorForPatientId() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(""))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors").isArray())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("patientId"));
        }

        @Test
        @DisplayName("never calls the use case when request validation fails")
        void shouldNotInvokeUseCaseOnValidationError() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(""))))
                    .andExpect(status().isBadRequest());

            verify(createClinicalRecordUseCase, never()).execute(PATIENT_ID);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/clinical-records — business rule violations")
    class BusinessRuleViolations {

        @Test
        @DisplayName("returns 422 Unprocessable Entity when a clinical record already exists for the patient")
        void shouldReturn422WhenRecordAlreadyExists() throws Exception {
            when(createClinicalRecordUseCase.execute(PATIENT_ID))
                    .thenThrow(new ClinicalRecordAlreadyExistsException(PATIENT_ID));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("response body contains the domain error message on duplicate patient")
        void shouldIncludeDomainMessageOnDuplicate() throws Exception {
            when(createClinicalRecordUseCase.execute(PATIENT_ID))
                    .thenThrow(new ClinicalRecordAlreadyExistsException(PATIENT_ID));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/clinical-records — unexpected errors")
    class PostUnexpectedErrors {

        @Test
        @DisplayName("returns 500 Internal Server Error on unhandled runtime exception")
        void shouldReturn500OnUnexpectedError() throws Exception {
            when(createClinicalRecordUseCase.execute(PATIENT_ID))
                    .thenThrow(new RuntimeException("Unexpected infrastructure failure"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreateClinicalRecordRequest(PATIENT_ID))))
                    .andExpect(status().isInternalServerError());
        }
    }

    // =========================================================================
    // GET /api/v1/clinical-records/{patientId}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/clinical-records/{patientId} — successful retrieval")
    class SuccessfulRetrieval {

        @Test
        @DisplayName("returns 200 OK with patientId in response body")
        void shouldReturn200WithPatientId() throws Exception {
            ClinicalRecord record = new ClinicalRecord(RECORD_ID, PATIENT_ID);
            when(getClinicalRecordUseCase.execute(PATIENT_ID)).thenReturn(record);

            mockMvc.perform(get(BASE_URL + "/{patientId}", PATIENT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.patientId").value(PATIENT_ID));
        }

        @Test
        @DisplayName("returns 200 OK with id in response body")
        void shouldReturn200WithId() throws Exception {
            ClinicalRecord record = new ClinicalRecord(RECORD_ID, PATIENT_ID);
            when(getClinicalRecordUseCase.execute(PATIENT_ID)).thenReturn(record);

            mockMvc.perform(get(BASE_URL + "/{patientId}", PATIENT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(RECORD_ID));
        }

        @Test
        @DisplayName("delegates to GetClinicalRecordUseCase with the patientId from the path")
        void shouldDelegateToGetUseCaseWithPatientIdFromPath() throws Exception {
            ClinicalRecord record = new ClinicalRecord(RECORD_ID, PATIENT_ID);
            when(getClinicalRecordUseCase.execute(PATIENT_ID)).thenReturn(record);

            mockMvc.perform(get(BASE_URL + "/{patientId}", PATIENT_ID))
                    .andExpect(status().isOk());

            verify(getClinicalRecordUseCase, times(1)).execute(PATIENT_ID);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/clinical-records/{patientId} — resource not found")
    class ResourceNotFound {

        @Test
        @DisplayName("returns 404 Not Found when no clinical record exists for the patient")
        void shouldReturn404WhenClinicalRecordNotFound() throws Exception {
            when(getClinicalRecordUseCase.execute(PATIENT_ID))
                    .thenThrow(new ClinicalRecordNotFoundException(PATIENT_ID));

            mockMvc.perform(get(BASE_URL + "/{patientId}", PATIENT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("response body contains the domain error message on missing record")
        void shouldIncludeDomainMessageWhenNotFound() throws Exception {
            when(getClinicalRecordUseCase.execute(PATIENT_ID))
                    .thenThrow(new ClinicalRecordNotFoundException(PATIENT_ID));

            mockMvc.perform(get(BASE_URL + "/{patientId}", PATIENT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Clinical record not found for patient: " + PATIENT_ID));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/clinical-records/{patientId} — unexpected errors")
    class GetUnexpectedErrors {

        @Test
        @DisplayName("returns 500 Internal Server Error on unhandled runtime exception")
        void shouldReturn500OnUnexpectedError() throws Exception {
            when(getClinicalRecordUseCase.execute(PATIENT_ID))
                    .thenThrow(new RuntimeException("Unexpected infrastructure failure"));

            mockMvc.perform(get(BASE_URL + "/{patientId}", PATIENT_ID))
                    .andExpect(status().isInternalServerError());
        }
    }
}
