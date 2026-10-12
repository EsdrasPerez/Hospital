package com.example.hospital.facturas.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public record FacturasDTO(
    Long idFactura,
    Long idPaciente,
    Long idHospitalizacion,
    LocalDateTime fechaEmision,
    BigDecimal subtotal,
    BigDecimal iva,
    BigDecimal total,
    Long idMetodoPago,
    Long idEstadoFactura,
    Boolean activo
) {}
