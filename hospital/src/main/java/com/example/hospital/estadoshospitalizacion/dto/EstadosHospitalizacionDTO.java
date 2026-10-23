package com.example.hospital.estadoshospitalizacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstadosHospitalizacionDTO(
    Long idEstadoHosp,
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 30, message = "La descripción no puede superar los 30 caracteres")
    String descripcion,
    Boolean activo
) {}
