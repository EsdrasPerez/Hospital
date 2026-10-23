package com.example.hospital.medicamentos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MedicamentosDTO(
    Long idMedicamento,
    @NotBlank(message = "El nombre comercial es obligatorio")
    @Size(max = 150, message = "El nombre comercial no puede superar los 150 caracteres")
    String nombreComercial,
    @Size(max = 150, message = "El nombre genérico no puede superar los 150 caracteres")
    String nombreGenerico,
    @Size(max = 100, message = "La presentación no puede superar los 100 caracteres")
    String presentacion,
    @Size(max = 100, message = "El laboratorio no puede superar los 100 caracteres")
    String laboratorio,
    // Solo se usa como stock inicial en POST; PUT lo ignora (cambia solo con PATCH /{id}/stock).
    @PositiveOrZero(message = "El stock no puede ser negativo")
    Integer stock,
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    Integer stockMinimo,
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", inclusive = false, message = "El precio debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales")
    BigDecimal precio,
    Boolean requiereReceta,
    Boolean activo
) {}
