package com.example.hospital.usuarios;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    private String nombreUsuario;
    private String contrasenaHash;
    private Boolean estado;
    private java.time.LocalDateTime ultimoAcceso;
    private Long idRol;

}
