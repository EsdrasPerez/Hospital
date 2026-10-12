package com.example.hospital.diagnosticos;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "diagnosticos")
@Data
public class Diagnosticos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDiagnostico;
    private String observaciones;
    private Long idTipoDiagnostico;
    private String descripcion;
    private java.time.LocalDate fecha;
    private Boolean activo;

}
