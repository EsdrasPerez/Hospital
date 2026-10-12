package com.example.hospital.hospitalizaciones;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "hospitalizaciones")
@Data
public class Hospitalizaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHospitalizacion;
    private Long idPaciente;
    private Long idHabitacion;
    private Long idMedicoResponsable;
    private Long idClinica;
    private java.time.LocalDateTime fechaIngreso;
    private java.time.LocalDateTime fechaAlta;
    private String motivoIngreso;
    private String diagnosticoIngreso;
    private Long idEstadoHosp;
    private Boolean activo;

}
