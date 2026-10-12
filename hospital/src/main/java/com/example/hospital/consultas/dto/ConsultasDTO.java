package com.example.hospital.consultas.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
public record ConsultasDTO(
    Long idConsulta,
    java.time.LocalDateTime fechaHora,
    String sintomas,
    java.math.BigDecimal peso,
    java.math.BigDecimal talla,
    java.math.BigDecimal temperatura,
    String presionArterial,
    Integer frecuenciaCardiaca,
    Long idCita,
    Boolean activo
) {}
