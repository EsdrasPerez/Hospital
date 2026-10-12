package com.example.hospital.usuarios.dto;

import java.time.LocalDateTime;

public record UsuariosDTO(
    Long idUsuario,
    String nombreUsuario,
    String password,
    Boolean estado,
    LocalDateTime ultimoAcceso,
    Long idRol
) {
    // Constructor de conveniencia para respuestas (sin exponer la contraseña)
    public static UsuariosDTO sinPassword(Long idUsuario, String nombreUsuario, Boolean estado, LocalDateTime ultimoAcceso, Long idRol) {
        return new UsuariosDTO(idUsuario, nombreUsuario, null, estado, ultimoAcceso, idRol);
    }
}
