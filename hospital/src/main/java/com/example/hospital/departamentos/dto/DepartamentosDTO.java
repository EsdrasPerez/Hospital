package com.example.hospital.departamentos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartamentosDTO(
    Long idDepartamento,
    @NotBlank(message = "El nombre del departamento es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    String nombre,
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    String descripcion,
    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    String telefono,
    Boolean estado
) {}
