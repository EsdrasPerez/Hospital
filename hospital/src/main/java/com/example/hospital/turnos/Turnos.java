package com.example.hospital.turnos;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "turnos")
@Data
public class Turnos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTurno;
    private String descripcion;
    private java.time.LocalTime horaInicio;
    private java.time.LocalTime horaFin;
    private Boolean activo;

}
