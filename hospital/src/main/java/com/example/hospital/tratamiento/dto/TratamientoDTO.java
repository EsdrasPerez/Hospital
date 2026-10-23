package com.example.hospital.tratamiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TratamientoDTO(
    Long idTratamiento,
    @NotBlank(message = "La descripción del tratamiento es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    String descripcion,
    @Positive(message = "La duración en días debe ser mayor a cero")
    Integer duracionDias,
    String indicaciones,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    @NotNull(message = "El diagnóstico asociado es obligatorio")
    Long idDiagnostico,
    Boolean activo
) {}
