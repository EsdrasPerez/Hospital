package com.example.hospital.clinicas.dto;

public record ClinicasDTO(
    Long idClinica,
    String nombre,
    String descripcion,
    String ubicacion,
    String telefono,
    Boolean estadoClinica,
    Long idDepartamento,
    Long idEspecialidad
) {}
