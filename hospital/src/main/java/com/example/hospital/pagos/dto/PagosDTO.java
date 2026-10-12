package com.example.hospital.pagos.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
public record PagosDTO(
    Long idPago,
    Long idFactura,
    Long idCuenta,
    Long idMetodoPago,
    java.math.BigDecimal monto,
    java.time.LocalDateTime fechaPago,
    String referencia,
    Boolean activo
) {}
