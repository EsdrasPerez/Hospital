package com.example.hospital.diagnosticoconsulta.dto;

import jakarta.validation.constraints.NotNull;

public record DiagnosticoConsultaDTO(
    @NotNull(message = "El diagnóstico es obligatorio")
    Long idDiagnostico,
    @NotNull(message = "La consulta es obligatoria")
    Long idConsulta
) {}
