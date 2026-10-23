package com.example.hospital.nivelesurgencia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record NivelesUrgenciaDTO(
    Long idNivelUrgencia,
    @NotBlank(message = "El código es obligatorio")
    @Size(max = 5, message = "El código no puede superar los 5 caracteres")
    @Pattern(regexp = "(?i)N[0-9]+", message = "El código debe tener el formato N1, N2, N3...")
    String codigo,
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 100, message = "La descripción no puede superar los 100 caracteres")
    String descripcion,
    @NotNull(message = "El tiempo máximo de espera es obligatorio")
    @PositiveOrZero(message = "El tiempo máximo de espera debe ser mayor o igual a 0")
    Integer tiempoMaxEsperaMin,
    @Size(max = 20, message = "El color no puede superar los 20 caracteres")
    String color,
    Boolean activo
) {}
