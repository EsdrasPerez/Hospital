package com.example.hospital.tiposhabitacion.dto;

import java.math.BigDecimal;
public record TiposHabitacionDTO(
    Long idTipoHabitacion,
    String descripcion,
    java.math.BigDecimal costoDia,
    Boolean activo
) {}
