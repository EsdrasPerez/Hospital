package com.example.hospital.triaje.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TriajeDTO(
    Long idTriaje,
    @NotBlank(message = "El nivel de emergencia es obligatorio")
    @Size(max = 50, message = "El nivel de emergencia no puede superar 50 caracteres")
    String nivelDeEmergencia,
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    String descripcion,
    Boolean activo
) {}
