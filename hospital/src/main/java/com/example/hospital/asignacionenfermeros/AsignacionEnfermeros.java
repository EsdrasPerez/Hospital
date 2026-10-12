package com.example.hospital.asignacionenfermeros;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "asignacion_enfermeros")
@Data
public class AsignacionEnfermeros {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAsignacion;
    private Long idEnfermero;
    private Long idHospitalizacion;
    private java.time.LocalDate fechaAsignacion;
    private Long idTurno;
    private Boolean activo;

}
