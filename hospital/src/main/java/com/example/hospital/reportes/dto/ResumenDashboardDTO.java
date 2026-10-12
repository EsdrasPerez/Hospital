package com.example.hospital.reportes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Indicadores generales del hospital para el tablero principal")
public record ResumenDashboardDTO(
        @Schema(description = "Fecha a la que corresponden los indicadores diarios", example = "2026-10-12")
        LocalDate fecha,

        @Schema(description = "Pacientes activos registrados", example = "245")
        long pacientesActivos,

        @Schema(description = "Citas activas programadas para la fecha", example = "38")
        long citasDelDia,

        @Schema(description = "Hospitalizaciones en curso (estado activo)", example = "17")
        long hospitalizacionesActivas,

        @Schema(description = "Pacientes en emergencia que aún no han sido dados de alta", example = "5")
        long emergenciasEnCurso,

        @Schema(description = "Habitaciones activas en estado Disponible", example = "22")
        long habitacionesDisponibles,

        @Schema(description = "Medicamentos activos con stock igual o por debajo del mínimo", example = "4")
        long medicamentosBajoStock,

        @Schema(description = "Facturas activas en estado Pendiente", example = "11")
        long facturasPendientes
) {}
