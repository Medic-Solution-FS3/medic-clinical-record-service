package com.medic.clinical_record.presentation.exceptions;

import com.medic.clinical_record.domain.BusinessValidationException;
import com.medic.clinical_record.domain.DomainException;
import com.medic.clinical_record.domain.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturn404WhenResourceNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("ClinicalRecord not found with identifier: abc-123")))
                .andExpect(jsonPath("$.path", is("/test/not-found")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void shouldReturn422WhenBusinessValidationFails() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.error", is("Unprocessable Entity")))
                .andExpect(jsonPath("$.message", is("Patient already has an active clinical record")));
    }

    @Test
    void shouldReturn400WhenGenericDomainExceptionThrown() throws Exception {
        mockMvc.perform(get("/test/domain-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Generic domain rule violated")));
    }

    @Test
    void shouldReturn500WithOpaqueMessageWhenUnexpectedError() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message",
                        is("An unexpected internal error occurred. Please try again later.")));
    }

    @Test
    void shouldReturn400WithFieldErrorsWhenValidationFails() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Request validation failed")))
                .andExpect(jsonPath("$.fieldErrors", hasSize(1)))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("name")));
    }

    @Test
    void shouldNotExposeStackTraceIn500Response() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    // ---- Test-only controller (not part of production code) ----

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/not-found")
        String notFound() {
            throw new ResourceNotFoundException("ClinicalRecord", "abc-123");
        }

        @GetMapping("/business-error")
        String businessError() {
            throw new BusinessValidationException("Patient already has an active clinical record");
        }

        @GetMapping("/unexpected")
        String unexpected() {
            throw new RuntimeException("Something went very wrong internally");
        }

        @GetMapping("/domain-error")
        String domainError() {
            throw new TestDomainException("Generic domain rule violated");
        }

        @PostMapping("/validate")
        String validate(@Valid @RequestBody ValidatedRequest request) {
            return request.name();
        }

        record ValidatedRequest(@NotBlank String name) {}
    }

    /** Concrete subclass used to exercise the generic {@code DomainException} handler directly. */
    static final class TestDomainException extends DomainException {
        TestDomainException(String message) {
            super(message);
        }
    }
}
