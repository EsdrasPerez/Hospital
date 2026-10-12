package com.example.hospital.estadosfactura.dto;

public record EstadosFacturaDTO(
    Long idEstadoFactura,
    String descripcion,
    Boolean activo
) {}
