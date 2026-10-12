package com.example.hospital.cuentas.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
public record CuentasDTO(
    Long idCuenta,
    Long idPaciente,
    Long idHospitalizacion,
    java.time.LocalDateTime fechaApertura,
    String estado,
    java.math.BigDecimal saldo,
    Boolean activo
) {}
