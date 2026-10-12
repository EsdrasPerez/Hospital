package com.example.hospital.medico.dto;

public record MedicoDTO(
    Long idMedico,
    String noColegiado,
    String nombre,
    String apellidoPaterno,
    String apellidoMaterno,
    String telefono,
    String email,
    Boolean estado
) {}
