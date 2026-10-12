package com.example.hospital.estadocita;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "estado_cita")
@Data
public class EstadoCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstado;
    private String nombre;
    private String descripcion;
    private Boolean activo;

}
