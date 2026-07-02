package com.medic.clinical_record.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void shouldExposeMessageAndCauseWhenBothProvided() {
        RuntimeException cause = new RuntimeException("root cause");

        DomainException exception = new DomainException("wrapped failure", cause) {};

        assertThat(exception.getMessage()).isEqualTo("wrapped failure");
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
