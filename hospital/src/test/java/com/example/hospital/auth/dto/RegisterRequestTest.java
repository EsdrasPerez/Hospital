package com.example.hospital.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rolEsObligatorio() {
        assertThat(validator.validate(new RegisterRequest("u", "Clave1234", null)))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("rol");
        assertThat(validator.validate(new RegisterRequest("u", "Clave1234", " "))).hasSize(1);
    }

    @Test
    void requestCompletoEsValido() {
        assertThat(validator.validate(new RegisterRequest("u", "Clave1234", "MEDICO"))).isEmpty();
    }

    @Test
    void laContrasenaDebeTenerAlMenosOchoCaracteres() {
        assertThat(validator.validate(new RegisterRequest("u", "1234567", "MEDICO")))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("password");
        assertThat(validator.validate(new RegisterRequest("u", "12345678", "MEDICO"))).isEmpty();
        assertThat(validator.validate(new RegisterRequest("u", "x".repeat(101), "MEDICO"))).hasSize(1);
    }

    @Test
    void usuarioYContrasenaSonObligatorios() {
        assertThat(validator.validate(new RegisterRequest(" ", null, "MEDICO")))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("username", "password");
    }
}
