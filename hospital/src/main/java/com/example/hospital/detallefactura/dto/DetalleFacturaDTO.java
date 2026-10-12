package com.example.hospital.detallefactura.dto;

import java.math.BigDecimal;
public record DetalleFacturaDTO(
    Long idDetalle,
    Long idFactura,
    String concepto,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal importe
) {}
