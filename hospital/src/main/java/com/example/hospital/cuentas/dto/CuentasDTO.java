package com.example.hospital.cuentas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record CuentasDTO(
    Long idCuenta,
    @NotNull(message = "El paciente es obligatorio")
    Long idPaciente,
    Long idHospitalizacion,
    @Schema(description = "Solo lectura: la asigna el servidor al abrir la cuenta.", accessMode = Schema.AccessMode.READ_ONLY)
    java.time.LocalDateTime fechaApertura,
    @Schema(description = "Abierta, Cerrada o Liquidada. Al abrir se ignora (nace Abierta). En PUT, Liquidada exige saldo 0.",
            allowableValues = {"Abierta", "Cerrada", "Liquidada"})
    @Pattern(regexp = "(?i)Abierta|Cerrada|Liquidada", message = "El estado debe ser Abierta, Cerrada o Liquidada")
    String estado,
    @Schema(description = "Saldo inicial al abrir (>= 0, por defecto 0). En PUT se ignora: el saldo solo cambia con `PATCH /{id}/ajustar-saldo`.")
    @PositiveOrZero(message = "El saldo no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El saldo admite hasta 8 enteros y 2 decimales")
    java.math.BigDecimal saldo,
    @Schema(description = "Se ignora: la cuenta nace activa y se desactiva con DELETE.")
    Boolean activo
) {}
