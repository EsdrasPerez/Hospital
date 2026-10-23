package com.example.hospital.diagnosticos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record DiagnosticosDTO(
    Long idDiagnostico,
    String observaciones,
    @NotNull(message = "El tipo de diagnóstico es obligatorio")
    Long idTipoDiagnostico,
    @NotBlank(message = "La descripción del diagnóstico es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    String descripcion,
    @PastOrPresent(message = "La fecha del diagnóstico no puede ser futura")
    LocalDate fecha,
    Boolean activo
) {}
