package com.example.hospital.tratamientomedicamento.dto;

import jakarta.validation.constraints.NotNull;

public record TratamientoMedicamentoDTO(
    @NotNull(message = "El medicamento es obligatorio")
    Long idMedicamento,
    @NotNull(message = "El tratamiento es obligatorio")
    Long idTratamiento
) {}
