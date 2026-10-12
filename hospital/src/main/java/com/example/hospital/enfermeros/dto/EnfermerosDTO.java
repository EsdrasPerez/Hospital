package com.example.hospital.enfermeros.dto;

public record EnfermerosDTO(
    Long idEnfermero,
    String cedulaProfesional,
    String nombre,
    String apellidoPaterno,
    String apellidoMaterno,
    String telefono,
    Long idDepartamento,
    Boolean activo
) {}
