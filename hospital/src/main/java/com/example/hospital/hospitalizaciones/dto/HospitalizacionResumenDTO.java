package com.example.hospital.hospitalizaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Resumen mínimo de una hospitalización para elegirla en caja (facturas y cuentas).
 * NO incluye motivo de ingreso, diagnóstico, médico ni clínica.
 */
@Schema(description = "Resumen mínimo de una hospitalización (sin motivo ni diagnóstico)")
public record HospitalizacionResumenDTO(
        Long idHospitalizacion,
        LocalDateTime fechaIngreso,
        LocalDateTime fechaAlta,
        @Schema(description = "Descripción del estado (Activa, Alta, ...)")
        String estado,
        @Schema(description = "Número de la habitación")
        String numeroHabitacion
) {}
