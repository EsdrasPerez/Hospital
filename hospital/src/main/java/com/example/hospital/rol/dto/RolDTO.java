package com.example.hospital.rol.dto;

public record RolDTO(
    Long idRol,
    String nombre,
    String descripcion,
    Boolean activo
) {}
