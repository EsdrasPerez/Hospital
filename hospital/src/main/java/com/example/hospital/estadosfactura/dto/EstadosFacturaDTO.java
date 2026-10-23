package com.example.hospital.estadosfactura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstadosFacturaDTO(
    Long idEstadoFactura,
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 30, message = "La descripción no puede superar los 30 caracteres")
    String descripcion,
    Boolean activo
) {}
