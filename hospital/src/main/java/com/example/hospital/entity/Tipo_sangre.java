package com.example.hospital.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tipo_sangre")
@Data
public class Tipo_sangre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTipoSangre;
    private String nombre;
    private String descripcion;
}
