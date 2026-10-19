package com.example.hospital.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rolEsObligatorio() {
        assertThat(validator.validate(new RegisterRequest("u", "x", null)))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("rol");
        assertThat(validator.validate(new RegisterRequest("u", "x", " "))).hasSize(1);
    }

    @Test
    void requestCompletoEsValido() {
        assertThat(validator.validate(new RegisterRequest("u", "x", "MEDICO"))).isEmpty();
    }
}
