package com.example.hospital.estadohabitacion;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "estado_habitacion")
@Data
public class EstadoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstadoHabitacion;
    private String nombre;
    private String descripcion;
    private Boolean activo;

}
