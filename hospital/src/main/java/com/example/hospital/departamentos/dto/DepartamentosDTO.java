package com.example.hospital.departamentos.dto;

public record DepartamentosDTO(
    Long idDepartamento,
    String nombre,
    String descripcion,
    String telefono,
    Boolean estado
) {}
