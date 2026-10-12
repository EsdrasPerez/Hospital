package com.example.hospital.paciente.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PacienteRequest(
        @Schema(example = "2345678901234")
        @NotBlank(message = "El DPI es obligatorio")
        @Pattern(regexp = "\\d{13}", message = "El DPI debe tener 13 dígitos")
        String dpiPaciente,

        @Schema(example = "Ana")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(max = 100)
        String apellidoPaterno,

        @NotBlank(message = "El apellido materno es obligatorio")
        @Size(max = 100)
        String apellidoMaterno,

        @Past(message = "La fecha de nacimiento debe ser pasada")
        LocalDate fechaNacimiento,

        @Schema(example = "F", description = "M o F")
        @NotBlank(message = "El sexo es obligatorio")
        @Pattern(regexp = "[MF]", message = "El sexo debe ser M o F")
        String sexo,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 20)
        String telefono,

        @Email(message = "Correo electrónico inválido")
        @Size(max = 150)
        String correoElectronico,

        @Size(max = 255)
        String direccion,

        Long idAlergias,
        Long idTipoDeSangre
) {}
