package com.example.hospital.especialidadmedico.dto;

public record EspecialidadMedicoDTO(
    Long idEspecialidad,
    String nombre,
    String descripcion,
    Boolean activo
) {}
