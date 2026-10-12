package com.example.hospital.facturas.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Estado de cuenta de una factura: total, lo abonado y lo que falta por pagar")
public record SaldoFacturaDTO(
        @Schema(description = "ID de la factura", example = "15")
        Long idFactura,

        @Schema(description = "ID del paciente al que pertenece la factura", example = "3")
        Long idPaciente,

        @Schema(description = "Total de la factura (incluye IVA)", example = "1120.00")
        BigDecimal total,

        @Schema(description = "Suma de los pagos activos registrados para la factura", example = "500.00")
        BigDecimal totalPagado,

        @Schema(description = "Monto pendiente de pago (nunca negativo)", example = "620.00")
        BigDecimal saldoPendiente,

        @Schema(description = "Cantidad de pagos activos aplicados", example = "2")
        int cantidadPagos,

        @Schema(description = "true si la factura ya está completamente pagada", example = "false")
        boolean liquidada
) {}
