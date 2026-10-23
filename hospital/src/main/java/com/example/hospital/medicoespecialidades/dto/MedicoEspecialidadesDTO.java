package com.example.hospital.medicoespecialidades.dto;

import jakarta.validation.constraints.NotNull;

public record MedicoEspecialidadesDTO(
    @NotNull(message = "La especialidad es obligatoria")
    Long idEspecialidad,
    @NotNull(message = "El médico es obligatorio")
    Long idMedico
) {}
