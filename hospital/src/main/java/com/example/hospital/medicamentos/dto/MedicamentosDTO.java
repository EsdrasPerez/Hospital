package com.example.hospital.medicamentos.dto;

import java.math.BigDecimal;
public record MedicamentosDTO(
    Long idMedicamento,
    String nombreComercial,
    String nombreGenerico,
    String presentacion,
    String laboratorio,
    Integer stock,
    Integer stockMinimo,
    BigDecimal precio,
    Boolean requiereReceta,
    Boolean activo
) {}
