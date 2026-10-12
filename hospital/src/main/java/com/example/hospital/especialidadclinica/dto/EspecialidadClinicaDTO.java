package com.example.hospital.especialidadclinica.dto;

public record EspecialidadClinicaDTO(
    Long idEspecialidad,
    String nombre,
    String descripcion,
    Boolean activo
) {}
