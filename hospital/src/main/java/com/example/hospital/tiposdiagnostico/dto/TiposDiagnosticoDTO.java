package com.example.hospital.tiposdiagnostico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TiposDiagnosticoDTO(
    Long idTipoDiagnostico,
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 30, message = "La descripción no puede superar los 30 caracteres")
    String descripcion,
    Boolean activo
) {}
