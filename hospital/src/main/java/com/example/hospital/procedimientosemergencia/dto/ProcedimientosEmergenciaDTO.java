package com.example.hospital.procedimientosemergencia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ProcedimientosEmergenciaDTO(
    Long idProcedimiento,
    @NotNull(message = "La emergencia es obligatoria")
    Long idEmergencia,
    @NotBlank(message = "La descripción del procedimiento es obligatoria")
    @Size(max = 200, message = "La descripción no puede superar 200 caracteres")
    String descripcion,
    LocalDateTime fechaHora,
    @NotNull(message = "El médico es obligatorio")
    Long idMedico,
    String observaciones,
    Boolean activo
) {}
