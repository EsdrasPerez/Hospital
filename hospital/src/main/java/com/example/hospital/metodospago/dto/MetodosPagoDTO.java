package com.example.hospital.metodospago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MetodosPagoDTO(
    Long idMetodoPago,
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 50, message = "La descripción no puede superar los 50 caracteres")
    String descripcion,
    Boolean activo
) {}
