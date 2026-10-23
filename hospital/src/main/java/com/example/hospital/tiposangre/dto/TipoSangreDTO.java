package com.example.hospital.tiposangre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TipoSangreDTO(
    Long idTipoDeSangre,
    @NotBlank(message = "El nombre del tipo de sangre es obligatorio")
    @Size(max = 10, message = "El nombre no puede superar los 10 caracteres")
    @Pattern(regexp = "(?i)(A|B|AB|O)[+-]", message = "El tipo de sangre debe ser A+, A-, B+, B-, AB+, AB-, O+ u O-")
    String nombre,
    @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
    String descripcion,
    Boolean activo
) {}
