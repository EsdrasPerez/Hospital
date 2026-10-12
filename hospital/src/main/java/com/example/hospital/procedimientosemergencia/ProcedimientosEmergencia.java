package com.example.hospital.procedimientosemergencia;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "procedimientos_emergencia")
@Data
public class ProcedimientosEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProcedimiento;
    private Long idEmergencia;
    private String descripcion;
    private java.time.LocalDateTime fechaHora;
    private Long idMedico;
    private String observaciones;
    private Boolean activo;

}
