package com.example.hospital.tiposangre.dto;

public record TipoSangreDTO(
    Long idTipoDeSangre,
    String nombre,
    String descripcion,
    Boolean activo
) {}
