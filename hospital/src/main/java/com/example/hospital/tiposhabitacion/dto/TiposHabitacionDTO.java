package com.example.hospital.tiposhabitacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TiposHabitacionDTO(
    Long idTipoHabitacion,
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 50, message = "La descripción no puede superar los 50 caracteres")
    String descripcion,
    @NotNull(message = "El costo por día es obligatorio")
    @DecimalMin(value = "0.00", inclusive = false, message = "El costo por día debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El costo por día admite hasta 8 enteros y 2 decimales")
    java.math.BigDecimal costoDia,
    Boolean activo
) {}
