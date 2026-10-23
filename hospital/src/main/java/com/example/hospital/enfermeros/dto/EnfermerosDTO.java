package com.example.hospital.enfermeros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnfermerosDTO(
    Long idEnfermero,
    @NotBlank(message = "La cédula profesional es obligatoria")
    @Size(max = 30, message = "La cédula profesional no puede superar los 30 caracteres")
    String cedulaProfesional,
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String nombre,
    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 100, message = "El apellido paterno no puede superar los 100 caracteres")
    String apellidoPaterno,
    @Size(max = 100, message = "El apellido materno no puede superar los 100 caracteres")
    String apellidoMaterno,
    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    String telefono,
    @NotNull(message = "El departamento es obligatorio")
    Long idDepartamento,
    Boolean activo
) {}
