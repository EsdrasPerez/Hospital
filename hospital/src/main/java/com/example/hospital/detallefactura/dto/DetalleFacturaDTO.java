package com.example.hospital.detallefactura.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DetalleFacturaDTO(
    Long idDetalle,
    @NotNull(message = "La factura es obligatoria")
    Long idFactura,
    @NotBlank(message = "El concepto es obligatorio")
    @Size(max = 200, message = "El concepto no puede superar los 200 caracteres")
    String concepto,
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    @Max(value = 100000, message = "La cantidad no puede superar 100000")
    Integer cantidad,
    @NotNull(message = "El precio unitario es obligatorio")
    @PositiveOrZero(message = "El precio unitario no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio unitario admite hasta 8 enteros y 2 decimales")
    BigDecimal precioUnitario,
    @Schema(description = "Solo lectura: cantidad x precio unitario, lo calcula el servidor (se ignora lo que envíe el cliente).",
            accessMode = Schema.AccessMode.READ_ONLY)
    BigDecimal importe
) {}
