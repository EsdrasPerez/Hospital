package com.example.hospital.clinicas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClinicasDTO(
    Long idClinica,
    @NotBlank(message = "El nombre de la clínica es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String nombre,
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    String descripcion,
    @Size(max = 255, message = "La ubicación no puede superar los 255 caracteres")
    String ubicacion,
    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    String telefono,
    Boolean estadoClinica,
    @NotNull(message = "El departamento es obligatorio")
    Long idDepartamento,
    Long idEspecialidad
) {}
