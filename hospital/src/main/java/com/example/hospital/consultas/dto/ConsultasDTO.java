package com.example.hospital.consultas.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConsultasDTO(
    Long idConsulta,
    LocalDateTime fechaHora,
    String sintomas,
    @DecimalMin(value = "0", inclusive = false, message = "El peso debe ser mayor a cero")
    @Digits(integer = 3, fraction = 2, message = "El peso admite hasta 3 enteros y 2 decimales")
    BigDecimal peso,
    @DecimalMin(value = "0", inclusive = false, message = "La talla debe ser mayor a cero")
    @Digits(integer = 3, fraction = 2, message = "La talla admite hasta 3 enteros y 2 decimales")
    BigDecimal talla,
    @DecimalMin(value = "30.0", message = "La temperatura debe estar entre 30.0 y 45.0 °C")
    @DecimalMax(value = "45.0", message = "La temperatura debe estar entre 30.0 y 45.0 °C")
    @Digits(integer = 2, fraction = 1, message = "La temperatura admite como máximo 1 decimal")
    BigDecimal temperatura,
    @Size(max = 10, message = "La presión arterial no puede superar 10 caracteres (ej. 120/80)")
    String presionArterial,
    @Min(value = 20, message = "La frecuencia cardíaca debe estar entre 20 y 250 lpm")
    @Max(value = 250, message = "La frecuencia cardíaca debe estar entre 20 y 250 lpm")
    Integer frecuenciaCardiaca,
    @NotNull(message = "La cita médica es obligatoria")
    Long idCita,
    Boolean activo
) {}
