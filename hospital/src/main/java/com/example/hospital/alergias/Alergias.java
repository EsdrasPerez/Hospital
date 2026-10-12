package com.example.hospital.alergias;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "alergias")
@Data
public class Alergias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlergias;
    private String nombre;
    private String descripcion;
    private Boolean activo;

}
