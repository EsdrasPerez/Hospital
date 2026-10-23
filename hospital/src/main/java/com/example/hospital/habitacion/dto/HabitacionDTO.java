package com.example.hospital.habitacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record HabitacionDTO(
    Long idHabitacion,
    @NotBlank(message = "El número de habitación es obligatorio")
    @Size(max = 10, message = "El número no puede superar 10 caracteres")
    String numero,
    @NotNull(message = "El piso es obligatorio")
    Integer piso,
    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor que cero")
    Integer capacidad,
    @NotNull(message = "El tipo de habitación es obligatorio")
    Long idTipoHabitacion,
    @NotNull(message = "El estado de la habitación es obligatorio")
    Long idEstadoHabitacion,
    Boolean activo
) {}
